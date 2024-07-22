import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.hid4java.HidDevice;
import org.hid4java.HidException;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.usb4java.ConfigDescriptor;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.Interface;
import org.usb4java.InterfaceDescriptor;
import org.usb4java.LibUsb;

public class XrealHID {
    // 将这些值替换为你的设备的供应商ID和产品ID
    private static final short AIR_VID = 0x3318; // 示例供应商ID
    private static final short AIR_PID = 0x0428; // 示例产品ID

    private static final byte INTERFACE_NUMBER = 3; // 声明的接口号为3
    private static final int REPORT_SIZE = 64; // 根据实际报告的大小来设置
    private static final float TICK_LEN = (1.0f / 3906000.0f);

    public static void main(String[] args) throws HidException {
        XrealHID xrealHID = new XrealHID();
        boolean started = xrealHID.start();
        if (!started) {
            return;
        }

//        System.exit(0);

//        new Thread(()-> {
//            xrealHID.startTrack();
//            while (true) {
//                System.out.println(xrealHID.report);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }).start();

        McuPacket mcuPacket = new McuPacket(0x15, new byte[0]);
        byte[] rawPacketData = mcuPacket.serialize().get();
        System.out.println(rawPacketData.length);
        System.out.println(Arrays.toString(rawPacketData));

        int val = xrealHID.writeHidDevice(rawPacketData);
        System.out.println(val);

        if (val > 0) {
            byte[] data = new byte[64];
            for (int i = 0; i < 64; i++) {
                val = xrealHID.hidDevice.read(data);
                McuPacket p = McuPacket.deserialize(data).get();
                System.out.println(Arrays.toString(data));
                System.out.println(p);
                if (p.cmdId == 0x15) {
                    break;
                }
            }
            System.out.println("over");
        }

        xrealHID.shutdown();
    }

    private static Report parseReport(byte[] reportData) {
        ByteBuffer buffer = ByteBuffer.wrap(reportData);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // 假设数据是小端序

        // 根据给定的结构解析数据
        short unknown1 = buffer.getShort();
        short unknown2 = buffer.getShort();
        byte unknown3 = buffer.get();
        int some_counter1 = buffer.getInt();
        byte unknown4 = buffer.get();
        long unknown5 = buffer.getLong();
        byte unknown6 = buffer.get();
        short rate_pitch = buffer.getShort();
        byte unknown7 = buffer.get();
        short rate_roll = buffer.getShort();
        byte unknown8 = buffer.get();
        short rate_yaw = buffer.getShort();
        short unknown9 = buffer.getShort();
        int unknown10 = buffer.getInt();
        byte unknown11 = buffer.get();
        short rot_roll = buffer.getShort();
        byte unknown13 = buffer.get();
        short rot_pitch1 = buffer.getShort();
        byte unknown14 = buffer.get();
        short rot_pitch2 = buffer.getShort();
        short unknown15 = buffer.getShort();
        int unknown16 = buffer.getInt();
        short mag1 = buffer.getShort();
        short mag2 = buffer.getShort();
        short mag3 = buffer.getShort();
        int some_counter2 = buffer.getInt();
        int unknown17 = buffer.getInt();
        byte unknown18 = buffer.get();
        byte unknown19 = buffer.get();

        if (unknown1 != 0x0201) {
            System.out.println("Unknown1: " + unknown1);
            return null;
        }

//        // 在控制台打印报告信息
//        System.out.printf("Rate: %04x %04x %04x\n", rate_roll, rate_pitch, rate_yaw);
//        System.out.printf("Rot:  %04x %04x %04x\n", rot_roll, rot_pitch1, rot_pitch2);
//        System.out.printf("Mag:  %04x %04x %04x\n", mag1, mag2, mag3);

        Report report = new Report();
        report.ticker = some_counter1 & 0xffffffffL;
        report.ang_vel[0] = rate_pitch;
        report.ang_vel[1] = rate_roll;
        report.ang_vel[2] = rate_yaw;
        return report;
    }

    Thread thread = null;
    HidServices hidServices = null;
    HidDevice hidDevice = null;
    Report report = new Report();

    public boolean start() {
        // 初始化并启动HID服务
        hidServices = HidManager.getHidServices();
        hidServices.start();

//        while (!hidServices.getAttachedHidDevices().isEmpty()) {
//            List<HidDevice> used = hidServices.getAttachedHidDevices();
//            for (HidDevice hidDevice : used) {
//                if (hidDevice.getVendorId() == AIR_VID && hidDevice.getProductId() == AIR_PID) {
//                    try {
//                        System.out.println("需要关闭 " + hidDevice);
//                        hidDevice.close();
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//        }

        // 打开设备
        List<HidDevice> devices = hidServices.getAttachedHidDevices();
        List<HidDevice> attachDevices = new ArrayList<>();
        for (HidDevice device : devices) {
            if (device.isVidPidSerial(AIR_VID, AIR_PID, null) && device.getUsagePage() != 0xc00) {
                attachDevices.add(device);
            }
        }
        for (HidDevice device : attachDevices) {
            System.out.println(device);
        }
        System.out.println("attachDevices: " + attachDevices.size());
        hidDevice = attachDevices.get(0);
        System.out.println("open: " + hidDevice.open());
        if (hidDevice == null) {
            System.err.println("HID设备未找到!");
            return false;
        }

        return true;
    }

    public boolean startTrack() {
        // 准备要写入的数据 (magic payload)
        {
            byte[] magicPayload = {(byte) 0x00, (byte) 0xaa, (byte) 0xc5, (byte) 0xd1, (byte) 0x21, (byte) 0x42, (byte) 0x04, (byte) 0x00, (byte) 0x19, (byte) 0x01};

            // 写数据到设备
            int val = writeHidDevice(magicPayload);
            if (val > 0) {
                initAndStartThread();
            }
        }
        return false;
    }

    public int writeHidDevice(byte[] data) {
        int val = hidDevice.write(data, data.length, (byte) 0x00);
        if (val < 0) {
            System.err.println("写入失败: " + hidDevice.getLastErrorMessage());
        } else {
            System.out.println("成功写入 " + val + " 字节数据到设备.");
        }
        return val;
    }

    public RotationUpdater rotationUpdater = new RotationUpdater(Quaternion.IDENTITY);

    private void initAndStartThread() {
        thread = new Thread(() -> {
            {
                // 用于接收报告的缓冲区
                byte[] data = new byte[REPORT_SIZE * 2];

                int i = 1000000;

                long last_sample_tick = 0;
                double[] ang_vel = new double[3];

                while (true) {
                    // 读取报告
                    int val = hidDevice.read(data, 5000);
                    if (val < 0) {
                        System.err.println("读取失败: " + hidDevice.getLastErrorMessage());
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        System.out.println("成功读取 " + val + " 字节数据从设备.");
                        Report report = parseReport(data);
                        if (report == null) {
                            continue;
                        }

                        this.report = report;
                        {
                            // these scale and bias corrections are all rough guesses
//                            ang_vel[0] = (float) (report.ang_vel[0] + 15) * -0.001f;
//                            ang_vel[1] = (float) (report.ang_vel[2] - 6) * 0.001f;
//                            ang_vel[2] = (float) (report.ang_vel[1] + 15) * 0.001f;
                            ang_vel[0] = (float) (report.ang_vel[0]) * -0.001f;
                            ang_vel[1] = (float) (report.ang_vel[2]) * 0.001f;
                            ang_vel[2] = (float) (report.ang_vel[1]) * 0.001f;
                        }

                        {
                            long tick_delta = 3906;
                            if (last_sample_tick > 0)
                                tick_delta = report.ticker - last_sample_tick;

                            float dt = tick_delta * TICK_LEN;
                            last_sample_tick = report.ticker;

                            rotationUpdater.updateRotation(dt, new Vector3D(ang_vel[0], ang_vel[1], ang_vel[2]));
                        }
                    }
                    if (--i <= 0) {
                        break;
                    }
                }
            }
        });

        thread.start();
    }

    public boolean shutdown() {
        if (thread != null) {
            thread.stop();
        }

        if (hidDevice != null) {
            // 关闭HID设备
            hidDevice.close();

        }

        if (hidServices != null) {
            // 停止HID服务
            hidServices.shutdown();
        }

        return true;
    }
}

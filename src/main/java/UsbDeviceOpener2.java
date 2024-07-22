import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;

public class UsbDeviceOpener2 {
    // 将这些值替换为你的设备的供应商ID和产品ID
    private static final short AIR_VID = 0x3318; // 示例供应商ID
    private static final short AIR_PID = 0x0428; // 示例产品ID

    private static final byte INTERFACE_NUMBER = 4; // 声明的接口号

    public static List<HidDevice> openNrealEndpoint(int interfaceNumber) {
        // 获取hid服务
        HidServices hidServices = HidManager.getHidServices();
        hidServices.start();

        List<HidDevice> matchDevices = new ArrayList<>();
        // 遍历所有已知的HID设备
        for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
            if (!hidDevice.toString().contains("XREAL")) {
                continue;
            }
            System.out.println("FOUND: " + hidDevice.toString());
            if (hidDevice.getVendorId() == AIR_VID) {
                matchDevices.add(hidDevice);
            }
        }
        hidServices.shutdown();
        return matchDevices;
    }

    public static void main(String[] args) {
        List<HidDevice> devices = openNrealEndpoint(INTERFACE_NUMBER);

        for (int id = 0; id < 4; id++) {
            HidDevice device = devices.get(id);
            if (!device.open()) {
                System.out.println("设备打开失败");
            }
            System.out.println(device);

            McuPacket mcuPacket = new McuPacket(0x15, new byte[0]);
            byte[] rawPacketData = mcuPacket.serialize().get();
            System.out.println(rawPacketData.length);
            System.out.println(Arrays.toString(rawPacketData));

            rawPacketData = new byte[] {(byte) 0x00, (byte) 0xaa, (byte) 0xc5, (byte) 0xd1, (byte) 0x21, (byte) 0x42, (byte) 0x04, (byte) 0x00, (byte) 0x19, (byte) 0x01};
            int val = writeHidDevice(device, rawPacketData);
            System.out.println("write: " + val);

            if (val > 0) {
                byte[] data = new byte[64];
                for (int i = 0; i < 2; i++) {
                    val = device.read(data, 5000);
                    System.out.println("read: " + val);
                    if (val > 0) {
                        McuPacket p = McuPacket.deserialize(data).get();
                        System.out.println(Arrays.toString(data));
                        System.out.println(p);
                        if (p.cmdId == 0x15) {
                            break;
                        }
                    }
                }
                System.out.println("over");
            }
            device.close();
        }


    }

    public static int writeHidDevice(HidDevice hidDevice, byte[] data) {
        int val = hidDevice.write(data, data.length, (byte) 0x00);
        if (val < 0) {
            System.err.println("写入失败: " + hidDevice.getLastErrorMessage());
        } else {
            System.out.println("成功写入 " + val + " 字节数据到设备.");
        }
        return val;
    }
}

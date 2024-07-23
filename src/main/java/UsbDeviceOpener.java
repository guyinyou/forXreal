import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.usb4java.ConfigDescriptor;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.EndpointDescriptor;
import org.usb4java.Interface;
import org.usb4java.InterfaceDescriptor;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

public class UsbDeviceOpener {

    private static final int IMU_PID = 24578;
    private static final int IMU_VID = 11727;
    private static final int TIMEOUT = 500; // 超时时间（单位：毫秒）

    public static void main(String[] args) {
        Context context = new Context();
        int result = LibUsb.init(context);
        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Unable to initialize libusb.", result);
        }

        try {
            Device device = findDevice(context, IMU_VID, IMU_PID);
            if (device == null) {
                System.out.println("Device not found.");
                return;
            }
            DeviceDescriptor descriptor = new DeviceDescriptor();
            result = LibUsb.getDeviceDescriptor(device, descriptor);
            if (result != LibUsb.SUCCESS) {
                throw new LibUsbException("Unable to read device descriptor", result);
            }
            System.out.format("Found Device: VID=0x%04X, PID=0x%04X%n", descriptor.idVendor(), descriptor.idProduct());

            // Attempt to open the device
            DeviceHandle handle = new DeviceHandle();
            result = LibUsb.open(device, handle);
            if (result != LibUsb.SUCCESS) {
                throw new LibUsbException("Unable to open USB device", result);
            }
            try {
                // Use handle for your operations
                // e.g., claim interface, transfer data, etc.
                // 获取设备的第一个配置描述符
                ConfigDescriptor configDescriptor = new ConfigDescriptor();
                result = LibUsb.getActiveConfigDescriptor(device, configDescriptor);
                if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to get active config descriptor", result);
//                System.out.println(configDescriptor);
                try {
                    byte interfaceNumber = 1;

                    // 在操作设备之前声明接口
                    result = LibUsb.claimInterface(handle, interfaceNumber);
                    if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to claim interface", result);

                    try {
                        {
                            ByteBuffer buffer = ByteBuffer.allocateDirect(32); // 读取缓冲区，大小根据需要调整
                            IntBuffer transferred = IntBuffer.allocate(1); // 实际传输字节数
                            // 读取数据（在这里执行了批量传输）
                            buffer.clear();
//                            buffer.put(Hex.hexStringToBytes("5aa5000200253f60"));
                            buffer.put(Hex.hexStringToBytes("5aa5000200233de0"));
                            result = LibUsb.bulkTransfer(handle, (byte) 0x02, buffer, transferred, TIMEOUT);
                            int len = transferred.get();
                            if (result != LibUsb.SUCCESS) {
                                throw new LibUsbException("Bulk transfer failed", result);
                            }
                            System.out.println("Data write: " + len + " bytes.");
                        }
                        new Thread(()-> {
                            ByteBuffer readBuffer = ByteBuffer.allocateDirect(4 * 1024 * 1024); // 读取缓冲区，大小根据需要调整
                            IntBuffer transferred = IntBuffer.allocate(1); // 实际传输字节数
                            while (true) {
                                int readResult = LibUsb.bulkTransfer(handle, (byte) 0x81, readBuffer, transferred, TIMEOUT);
                                transferred.position(0);
                                int len = transferred.get();
                                if (readResult != LibUsb.SUCCESS) {
//                                    throw new RuntimeException("Error in bulk transfer (read): " + LibUsb.strError(readResult));
                                    System.err.println("Error in bulk transfer (read): " + LibUsb.strError(readResult));
                                    try {
                                        Thread.sleep(1);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                if (len != 38) {
                                    System.out.println("Bulk transfer (read) successful: " + len + " bytes transferred");
                                    System.err.println("Error in bulk transfer (read): " + LibUsb.strError(readResult));
                                    continue;
                                }

                                // 处理接收到的数据
                                byte[] data = new byte[len];
                                readBuffer.position(0);
                                readBuffer.get(data);
                                // 打印数据或进行其他处理...
                                AAA.ArknovvReport arknovvReport = AAA.processIMUData(data);
                                if (arknovvReport == null) {
                                    continue;
                                }
                                AAA.FUN_1800059e0(arknovvReport);
                                System.out.println(arknovvReport);
                                estimator.update(arknovvReport);
                            }
                        }).start();
                        Thread.sleep(300000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        // 释放接口
                        LibUsb.releaseInterface(handle, interfaceNumber);
                    }
                } finally {
                    // 释放资源
                    LibUsb.freeConfigDescriptor(configDescriptor);
                }
            } finally {
                LibUsb.close(handle);
            }
        } finally {
            LibUsb.exit(context);
        }
    }
    public static AttitudeEstimator estimator = new AttitudeEstimator();
    public static Device findDevice(Context context, int vendorId, int productId) {
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(context, list);
        if (result < 0) {
            throw new LibUsbException("Unable to get device list", result);
        }

        try {
            for (Device device: list) {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) {
                    throw new LibUsbException("Unable to read device descriptor", result);
                }
                if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) {
                    // Found our device
                    return device;
                }
            }
        } finally {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }

        // Device not found
        return null;
    }
}

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class AAA {
    public static void main(String[] args) {
        byte[] bytes = Hex.hexStringToBytes("5AA502001E24D56C1800000000009400F7FE5910FFFFF0FF0800FCFF26FFCBFF06000002AC65");
        System.out.println(Arrays.toString(bytes));
        System.out.println(bytes.length);

        ArknovvReport arknovvReport = processIMUData(bytes);
        System.out.println(arknovvReport);
    }

    private static final double PI = 3.141593;
    private static final double DEG_TO_RAD = 1.0 / 180.0;
    private static final double SCALE_1 = 16.4;
    private static final double SCALE_2 = 0.00024414062;
    private static final double GRAVITY = 9.8100004;
    public static ArknovvReport processIMUData(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        int flag1 = byteBuffer.getInt();
        short flag2 = byteBuffer.getShort();
        if (flag1 != 173402 || flag2 != 9246) {
            System.err.println("flag error");
            return null;
        }

        float v3 = (float)(byteBuffer.getShort(20));
        float v4 = (float)(byteBuffer.getShort(22));
        float v5 = (float)(byteBuffer.getShort(24));

        float v12 = (float)(byteBuffer.getShort(14));
        float v14 = (float)(byteBuffer.getShort(16));
        float v16 = (float)(byteBuffer.getShort(18));

        long timeStep = byteBuffer.getLong(6);
        float g_x = (float)((v3 / SCALE_1) * PI * DEG_TO_RAD);
        float g_y = (float)((v4 / SCALE_1) * PI * DEG_TO_RAD);
        float g_z = (float)((v5 / SCALE_1) * PI * DEG_TO_RAD);

        float a_x = (float)(v12 * SCALE_2 * GRAVITY);
        float a_y = (float)(v14 * SCALE_2 * GRAVITY);
        float a_z = (float)(v16 * SCALE_2 * GRAVITY);

        ArknovvReport arknovvReport = new ArknovvReport();
        arknovvReport.timestep = timeStep;
        arknovvReport.gyro_x = g_x;
        arknovvReport.gyro_y = g_y;
        arknovvReport.gyro_z = g_z;
        arknovvReport.acc_x = a_x;
        arknovvReport.acc_y = a_y;
        arknovvReport.acc_z = a_z;
        return arknovvReport;
    }
    public static class ArknovvReport {
        public long timestep;
        public float gyro_x;
        public float gyro_y;
        public float gyro_z;
        public float acc_x;
        public float acc_y;
        public float acc_z;

        @Override
        public String toString() {
            return "Report{" +
                "timestep=" + timestep +
                ", gyro_x=" + gyro_x +
                ", gyro_y=" + gyro_y +
                ", gyro_z=" + gyro_z +
                ", acc_x=" + acc_x +
                ", acc_y=" + acc_y +
                ", acc_z=" + acc_z +
                '}';
        }
    }
}
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class AAA {
    public static void main(String[] args) {
        byte[] bytes = Hex.hexStringToBytes("5AA502001E24020000000000000058FF01F0D2FFFBFFF1FF0800A1FEDBFE460000000000C196000000000000000064029CDE0000000132BD4000000000000000000000000000000000CD2080BF04E7CCBD0000000000000000000080BF000000");
        System.out.println(Arrays.toString(bytes));
        System.out.println(bytes.length);

        ArknovvReport arknovvReport = processIMUData(bytes);
        System.out.println(arknovvReport);
        FUN_1800059e0(arknovvReport);
        System.out.println(arknovvReport);
    }
    public static void Reverse(byte[] bytes) {
        for (int i = 0; i < bytes.length / 2; i++) {
            byte tmp = bytes[i];
            bytes[i] = bytes[bytes.length - i - 1];
            bytes[bytes.length - i - 1] = tmp;
        }
    }
    public static float p(String s) {
        byte[] bytes = Hex.hexStringToBytes(s);
        Reverse(bytes);
        float result = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        System.out.println(s + " : " + result);
        return result;
    }

    private static final double PI = 3.1415925;
    private static final double DEG_TO_RAD = 180.0;
    private static final double SCALE_1 = 16.4f;
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
        float g_x = (float)((v3 / SCALE_1) * PI / DEG_TO_RAD);
        float g_y = (float)((v4 / SCALE_1) * PI / DEG_TO_RAD);
        float g_z = (float)((v5 / SCALE_1) * PI / DEG_TO_RAD);

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



    /* WARNING: Globals starting with '_' overlap smaller symbols at the same address */

    public static void FUN_1800059e0(ArknovvReport report)
    {
        float fVar5;
        float fVar7;
        float fVar11;
        float fVar13;
        float fVar15;
        float fVar17;

        fVar5 = report.acc_y - (-0.0365279f);
        fVar7 = report.acc_x - (0.0080998605f);
        fVar15 = report.acc_z - (0.0620561f);
        fVar17 = report.gyro_y - (-0.015046221f);
        fVar11 = report.gyro_z - (0.008287932f);
        fVar13 = report.gyro_x - (-0.0034493743f);
        report.gyro_y = ((fVar17 * (1.0017928f) + fVar11 * (0.0035108617f) + fVar13 * (0.0036891531f)) * 180.0f) / 3.1415925f;
        report.acc_x = (fVar5 * (7.170662E-4f) + fVar15 * (3.7714484E-4f) + fVar7 * (0.99933827f)) / 9.81f;
        report.acc_y = (fVar15 * (0.0023892217f) + fVar5 * (1.0007114f) + fVar7 * 0f) / 9.81f;
        report.acc_z = (fVar15 * (1.0011235f) + fVar5 * 0f + fVar7 * 0f) / 9.81f;
        report.gyro_x = ((fVar17 * (-2.350146E-5f) + fVar11 * (0.0041440194f) + fVar13 * (1.0035535f)) * 180.0f) / 3.1415925f;
        report.gyro_z = ((fVar17 * (-0.002553038f) + fVar11 * (1.0021266f) + fVar13 * (-8.004126E-4f)) * 180.0f) / 3.1415925f;
    }

}
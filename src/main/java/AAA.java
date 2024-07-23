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
        FUN_1800059e0(arknovvReport);
        System.out.println(arknovvReport);
        System.out.println(0xBD159E47);
        System.out.println((float)0xBD159E47);
        System.out.println(p("BD159E47"));
        System.out.println(p("3C04B547"));
        System.out.println(p("3D7E2E8A"));
        System.out.println(p("BC76846D"));
        System.out.println(p("3A3BF980"));
        System.out.println(p("3F7FD4A2"));
        System.out.println(p("39C5BB86"));
        System.out.println(p("3F8024D1"));
        System.out.println(p("3B1C947D"));
        System.out.println(p("80000000"));
        System.out.println(p("3F801750"));
        System.out.println(p("3C07CA1B"));
        System.out.println(p("BB620EE6"));
        System.out.println(p("00000000"));
        System.out.println(p("80000000"));
        System.out.println(p("BB2750DF"));
        System.out.println(p("3F807471"));
        System.out.println(p("3B87CA8E"));
        System.out.println(p("B7C52500"));
        System.out.println(p("3F8045AF"));
        System.out.println(p("3B71C5B8"));
        System.out.println(p("3B66167C"));
        System.out.println(p("3F803ABF"));
        System.out.println(p("BA51D2C8"));

        System.out.println("3F800000: " + p("3F800000"));
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
//        Reverse(bytes);
        float result = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
        System.out.println(s + " : " + result);
        return result;
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



    /* WARNING: Globals starting with '_' overlap smaller symbols at the same address */

    public static void FUN_1800059e0(ArknovvReport report)
    {
        float fVar1;
        float fVar2;
        float fVar3;
        float fVar4;
        float fVar5;
        float fVar6;
        float fVar7;
        float fVar8;
        float fVar9;
        float fVar10;
        float fVar11;
        float fVar12;
        float fVar13;
        float fVar14;
        float fVar15;
        float fVar16;
        float fVar17;
        float fVar18;

        fVar5 = report.acc_y - p("BD159E47");
        fVar7 = report.acc_x - p("3C04B547");
        fVar15 = report.acc_z - p("3D7E2E8A");
        fVar17 = report.gyro_y - p("BC76846D");
        fVar10 = fVar5 * p("00000000");
        fVar3 = fVar7 * p("3F800000");
        fVar1 = fVar15 * p("00000000");
        fVar16 = fVar15 * p("3F800000");
        fVar15 = fVar15 * p("00000000");
        fVar6 = fVar5 * p("00000000");
        fVar5 = fVar5 * p("3F800000");
        fVar11 = report.gyro_z - p("3C07CA1B");
        fVar13 = report.gyro_x - p("BB620EE6");
        fVar8 = fVar7 * 0;
        fVar7 = fVar7 * p("00000000");
        fVar2 = fVar11 * p("BB2750DF");
        fVar9 = fVar17 * p("3F807471");
        fVar18 = fVar17 * p("3B87CA8E");
        fVar4 = fVar13 * p("3F800000");
        fVar12 = fVar11 * p("3F8045AF");
        fVar14 = fVar13 * p("00000000");
        report.gyro_y = ((fVar17 * p("3B66167C") + fVar11 * p("3F803ABF") + fVar13 * p("00000000")) * 180.0f) /
            3.141593f;
        report.acc_x = (fVar10 + fVar1 + fVar3) / 9.81f;
        report.acc_y = (fVar15 + fVar5 + fVar7) / 9.81f;
        report.acc_z = (fVar16 + fVar6 + fVar8) / 9.81f;
        report.gyro_x = ((fVar9 + fVar2 + fVar4) * 180.0f) / 3.141593f;
        report.gyro_z = ((fVar18 + fVar12 + fVar14) * 180.0f) / 3.141593f;
    }

}
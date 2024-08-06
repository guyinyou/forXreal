import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.lang.Math.PI;

public class AttitudeEstimator {
    private static final float Kp = 1.0f; // proportional gain governs rate of convergence to accelerometer/magnetometer

    private static final float Ki = 0.000f; // integral gain governs rate of convergence of gyroscope biases

    private static final float halfT = 0.001f * 0.5f; // half the sample period
    private float q0 = 0, q1 = 1, q2 = 0, q3 = 0; // quaternion elements representing the estimated orientation
    private float q0_ = 0, q1_ = 1, q2_ = 0, q3_ = 0; // quaternion elements representing the estimated orientation
    private long timestep = 0;
    private static float exInt = 0, eyInt = 0, ezInt = 0; // scaled integral error

    private float yaw = 0;
    private float pitch = 0;
    private float roll = 0;
    private static final float deg2Rad = 3.1415925f / 180f;

    public void update(AAA.ArknovvReport report) {
        this.IMU_Update(report.timestep, report.gyro_z, report.gyro_y, report.gyro_x, report.acc_x, report.acc_y, report.acc_z);
    }
    public void IMU_Update(long timestep, float gx, float gy, float gz, float ax, float ay, float az) {
        this.timestep = timestep;

        gx = gx * deg2Rad;
        gy = gy * deg2Rad;
        gz = gz * deg2Rad;

        float norm;
        float vx, vy, vz;
        float ex, ey, ez;

        float q0q0 = q0 * q0;
        float q0q1 = q0 * q1;
        float q0q2 = q0 * q2;
        float q1q1 = q1 * q1;
        float q1q3 = q1 * q3;
        float q2q2 = q2 * q2;
        float q2q3 = q2 * q3;
        float q3q3 = q3 * q3;

        if (ax * ay * az == 0) {
            return;
        }

        norm = (float) Math.sqrt(ax * ax + ay * ay + az * az); //
        ax = ax / norm;
        ay = ay / norm;
        az = az / norm;

        // estimated direction of gravity and flux (v and w)
        vx = 2 * (q1q3 - q0q2);
        vy = 2 * (q0q1 + q2q3);
        vz = q0q0 - q1q1 - q2q2 + q3q3;

        // error is sum of cross product between reference direction of fields and direction measured by sensors
        ex = (ay * vz - az * vy);
        ey = (az * vx - ax * vz);
        ez = (ax * vy - ay * vx);

        exInt = exInt + ex * Ki;
        eyInt = eyInt + ey * Ki;
        ezInt = ezInt + ez * Ki;

        // adjusted gyroscope measurements
        gx = gx + Kp * ex + exInt;
        gy = gy + Kp * ey + eyInt;
        gz = gz + Kp * ez + ezInt;

        // integrate quaternion rate and normalise
        q0 = q0 + (-q1 * gx - q2 * gy - q3 * gz) * halfT;
        q1 = q1 + (q0 * gx + q2 * gz - q3 * gy) * halfT;
        q2 = q2 + (q0 * gy - q1 * gz + q3 * gx) * halfT;
        q3 = q3 + (q0 * gz + q1 * gy - q2 * gx) * halfT;

        float bl = 7.142857142857143f * 1.5f;
        bl = 16.76f / 1;
        q0_ = q0 + (-q1 * gx - q2 * gy - q3 * gz) * halfT * bl;
        q1_ = q1 + (q0 * gx + q2 * gz - q3 * gy) * halfT * bl;
        q2_ = q2 + (q0 * gy - q1 * gz + q3 * gx) * halfT * bl;
        q3_ = q3 + (q0 * gz + q1 * gy - q2 * gx) * halfT * bl;

        // normalise quaternion
        norm = (float) Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 = q0 / norm;
        q1 = q1 / norm;
        q2 = q2 / norm;
        q3 = q3 / norm;

        norm = (float) Math.sqrt(q0_ * q0_ + q1_ * q1_ + q2_ * q2_ + q3_ * q3_);
        q0_ = q0_ / norm;
        q1_ = q1_ / norm;
        q2_ = q2_ / norm;
        q3_ = q3_ / norm;

//        yaw = (float) Math.atan2(2 * q1 * q2 + 2 * q0 * q3, -2 * q2 * q2 - 2 * q3 * q3 + 1) * 57.3f; // unit:degree
//        pitch = (float) Math.asin(-2 * q1 * q3 + 2 * q0 * q2) * 57.3f; // unit:degree
//        roll = (float) Math.atan2(2 * q2 * q3 + 2 * q0 * q1, -2 * q1 * q1 - 2 * q2 * q2 + 1) * 57.3f; // unit:degree
//        System.out.println(q0 + " " + q1 + " " + q2 + " " + q3);
//        System.out.println("yaw: " + yaw + " pitch: " + pitch + " roll: " + roll);
//        System.out.println(q0 + " " + q1 + " " + q2 + " " + q3);
    }

    ByteBuffer buffer = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN);
    public byte[] getQuaternion() {
        buffer.clear();
        buffer.putFloat(q0_);
        buffer.putFloat(q1_);
        buffer.putFloat(q2_);
        buffer.putFloat(q3_);
        buffer.putLong(this.timestep);
        return buffer.array();
    }
}

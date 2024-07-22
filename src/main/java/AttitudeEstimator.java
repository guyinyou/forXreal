public class AttitudeEstimator {
    private static final float Kp = 10.0f; // proportional gain governs rate of convergence to accelerometer/magnetometer
    private static final float Ki = 0.008f; // integral gain governs rate of convergence of gyroscope biases
    private static final float halfT = 0.001f; // half the sample period

    private static float q0 = 1, q1 = 0, q2 = 0, q3 = 0; // quaternion elements representing the estimated orientation
    private static float exInt = 0, eyInt = 0, ezInt = 0; // scaled integral error

    private static float yaw = 0;
    private static float pitch = 0;
    private static float roll = 0;

    public void update(AAA.ArknovvReport report) {
        IMU_Update(1, report.gyro_x, report.gyro_y, report.gyro_z, report.acc_x, report.acc_y, report.acc_z);
    }
    public void IMU_Update(float dt, float gx, float gy, float gz, float ax, float ay, float az) {
        gx *= dt;
        gy *= dt;
        gz *= dt;
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

        // normalise quaternion
        norm = (float) Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 = q0 / norm;
        q1 = q1 / norm;
        q2 = q2 / norm;
        q3 = q3 / norm;

        yaw = (float) Math.atan2(2 * q1 * q2 + 2 * q0 * q3, -2 * q2 * q2 - 2 * q3 * q3 + 1) * 57.3f; // unit:degree
        pitch = (float) Math.asin(-2 * q1 * q3 + 2 * q0 * q2) * 57.3f; // unit:degree
        roll = (float) Math.atan2(2 * q2 * q3 + 2 * q0 * q1, -2 * q1 * q1 - 2 * q2 * q2 + 1) * 57.3f; // unit:degree
        System.out.println("yaw:" + yaw + " pitch:" + pitch + " roll:" + roll);
    }
}

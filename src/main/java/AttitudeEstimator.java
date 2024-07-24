public class AttitudeEstimator {
    private static final float ALPHA = 1f; // 互补滤波器常数
    private long lastTimeStep = 0;

    // 四元数表示当前姿态
    private float[] q = {1.0f, 0.0f, 0.0f, 0.0f};



    public void update(AAA.ArknovvReport data) {
        if (lastTimeStep == 0) {
            lastTimeStep = data.timestep;
            return;
        }

        float dt = (data.timestep - lastTimeStep) / 1f; // 时间步长（假设单位为微秒）
        lastTimeStep = data.timestep;

        // 归一化加速度计数据
        float accNorm = (float) Math.sqrt(data.acc_x * data.acc_x + data.acc_y * data.acc_y + data.acc_z * data.acc_z);
        if (accNorm > 0) {
            data.acc_x /= accNorm;
            data.acc_y /= accNorm;
            data.acc_z /= accNorm;
        }

        // 使用陀螺仪数据更新四元数
        float[] qGyro = integrateGyro(q, data.gyro_x, data.gyro_y, data.gyro_z, dt);

        // 使用加速度计数据校正四元数
        float[] accOrientation = calculateAccQuaternion(data.acc_x, data.acc_y, data.acc_z);

        // 互补滤波器更新四元数
        q = new float[]{
            ALPHA * qGyro[0] + (1 - ALPHA) * accOrientation[0],
            ALPHA * qGyro[1] + (1 - ALPHA) * accOrientation[1],
            ALPHA * qGyro[2] + (1 - ALPHA) * accOrientation[2],
            ALPHA * qGyro[3] + (1 - ALPHA) * accOrientation[3]
        };

        // 归一化四元数
        normalizeQuaternion(q);

        // 输出欧拉角结果
        float[] eulerAngles = quaternionToEuler(q);
        System.out.printf("Roll: %.2f, Pitch: %.2f, Yaw: %.2f%n",
            Math.toDegrees(eulerAngles[0]), Math.toDegrees(eulerAngles[1]), Math.toDegrees(eulerAngles[2]));
    }

    private float[] integrateGyro(float[] q, float gyroX, float gyroY, float gyroZ, float dt) {
        // 将陀螺仪数据从度/秒转为弧度/秒
        float gyroXRad = (float) Math.toRadians(gyroX);
        float gyroYRad = (float) Math.toRadians(gyroY);
        float gyroZRad = (float) Math.toRadians(gyroZ);

        // 计算四元数微小旋转量
        float halfDt = 0.5f * dt;
        float[] dq = new float[]{
            0.0f,
            gyroXRad * halfDt,
            gyroYRad * halfDt,
            gyroZRad * halfDt
        };

        float[] qGyro = new float[4];
        qGyro[0] = q[0] - dq[1] * q[1] - dq[2] * q[2] - dq[3] * q[3];
        qGyro[1] = q[0] * dq[1] + q[1] * dq[0] + dq[2] * q[3] - dq[3] * q[2];
        qGyro[2] = q[0] * dq[2] - q[1] * dq[3] + q[2] * dq[0] + dq[3] * q[1];
        qGyro[3] = q[0] * dq[3] + dq[1] * q[2] - dq[2] * q[1] + q[3] * dq[0];

        // 归一化四元数
        normalizeQuaternion(qGyro);
        return qGyro;
    }

    private float[] calculateAccQuaternion(float accX, float accY, float accZ) {
        float accPitch = (float) Math.atan2(-accX, Math.sqrt(accY * accY + accZ * accZ));
        float accRoll = (float) Math.atan2(accY, accZ);

        float q0 = (float) Math.cos(accPitch / 2) * (float) Math.cos(accRoll / 2);
        float q1 = (float) Math.sin(accPitch / 2) * (float) Math.cos(accRoll / 2);
        float q2 = (float) Math.cos(accPitch / 2) * (float) Math.sin(accRoll / 2);
        float q3 = (float) Math.sin(accPitch / 2) * (float) Math.sin(accRoll / 2);

        return new float[]{q0, q1, q2, q3};
    }

    private float[] quaternionToEuler(float[] q) {
        float roll = (float) Math.atan2(2.0f * (q[0] * q[1] + q[2] * q[3]),
            1.0f - 2.0f * (q[1] * q[1] + q[2] * q[2]));
        float pitch = (float) Math.asin(2.0f * (q[0] * q[2] - q[3] * q[1]));
        float yaw = (float) Math.atan2(2.0f * (q[0] * q[3] + q[1] * q[2]),
            1.0f - 2.0f * (q[2] * q[2] + q[3] * q[3]));

        return new float[]{roll, pitch, yaw};
    }

    private void normalizeQuaternion(float[] q) {
        float norm = (float) Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
        q[0] /= norm;
        q[1] /= norm;
        q[2] /= norm;
        q[3] /= norm;
    }
}

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.complex.Quaternion;

public class RotationUpdater {

    // 假设的四元数表示当前的旋转
    private Quaternion rotation;
    // 用作旋转更新的锁对象
    private final Object lock = new Object();

    public RotationUpdater(Quaternion initialRotation) {
        this.rotation = initialRotation;
    }

    // 更新旋转
    public void updateRotation(float dt, Vector3D angVel) {
        synchronized (lock) {
            // 计算角速度向量的长度
            double angVelLength = angVel.getNorm();

            // 如果角速度长度不接近零，则执行旋转更新
            if (angVelLength > 0.00000001f) {
                // 计算旋转轴，即单位化的角速度向量
                Vector3D rotAxis = angVel.normalize();

                // 计算旋转角度，即角速度长度乘以时间增量
                double rotAngle = angVelLength * dt;

                // 创建四元数来表示增量旋转
                Quaternion deltaRotation = createFromAxisAngle(rotAxis, rotAngle);

                // 将现有旋转和增量旋转结合起来更新旋转
                rotation = rotation.multiply(deltaRotation);
            }

            // 归一化四元数以防止误差累积
            rotation = rotation.normalize();
        }
    }

    // 使用旋转轴和旋转角度创建四元数的示例方法
    // 注意：根据你使用的数学库的不同，该方法的实现会不同
    private Quaternion createFromAxisAngle(Vector3D axis, double angle) {
        // 根据你的四元数库，你可能需要实现该方法。
        // 下面是使用Apache Commons Math库的示例实现：
        double halfAngle = angle / 2.0;
        double sinHalfAngle = Math.sin(halfAngle);
        return new Quaternion(Math.cos(halfAngle), sinHalfAngle * axis.getX(),
            sinHalfAngle * axis.getY(), sinHalfAngle * axis.getZ());
    }

    // 获取当前的旋转
    public Quaternion getRotation() {
        synchronized (lock) {
            return rotation;
        }
    }

    public void resetRotation() {
        synchronized (lock) {
            rotation = Quaternion.IDENTITY;
        }
    }

    public Vector3D getRotationVector() {
        Rotation r = new Rotation(rotation.getQ0(), rotation.getQ1(), rotation.getQ2(), rotation.getQ3(), true);
        // 提取欧拉角
        double[] eulerAngles = r.getAngles(RotationOrder.XYZ);
        double roll = Math.round(Math.toDegrees(eulerAngles[0]) * 1000.0) / 1000.0;  // 绕X轴的旋转角度（度数）
        double pitch = Math.round(Math.toDegrees(eulerAngles[1]) * 1000.0) / 1000.0; // 绕Y轴的旋转角度（度数）
        double yaw = Math.round(Math.toDegrees(eulerAngles[2]) * 1000.0) / 1000.0;   // 绕Z轴的旋转角度（度数）
        return new Vector3D(roll, pitch, yaw);
    }
}

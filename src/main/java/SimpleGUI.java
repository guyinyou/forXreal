import java.awt.event.ActionEvent;
import java.util.Arrays;
import javax.swing.*;
import java.awt.*;
import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;

public class SimpleGUI {
    public static void main(String[] args) {
        // 在事件分派线程中创建和显示这个窗体
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    static XrealHID xrealHID = new XrealHID();
    private static void createAndShowGUI() {
        // 创建窗体并设置标题
        JFrame frame = new JFrame("Simple GUI with Text Fields");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 创建一个面板用于存放文本框
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // 创建三个文本框
        final JTextField textField1 = new JTextField("Text Field 1");
        final JTextField textField2 = new JTextField("Text Field 2");
        final JTextField textField3 = new JTextField("Text Field 3");

        final JButton button = new JButton();
        button.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("is click");
                xrealHID.rotationUpdater.resetRotation();
            }
        });

        // 将文本框添加到面板
        panel.add(textField1);
        panel.add(textField2);
        panel.add(textField3);
        panel.add(button);

        // 将面板添加到窗体
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        // 调整窗体大小以适应组件
        frame.pack();

        // 设置窗体可见
        frame.setVisible(true);

        // 模拟实时更新文本框内容的过程（仅作示例）
        new Thread(new Runnable() {
            @Override
            public void run() {
//                int counter = 0;
//                while (true) {
//                    try {
//                        // 更新文本框内容
//                        textField1.setText("Counter: " + counter);
//                        textField2.setText("Time: " + System.currentTimeMillis());
//                        textField3.setText("Random: " + Math.random());
//
//                        // 增加计数器
//                        counter++;
//
//                        // 线程休眠1秒
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
                boolean started = xrealHID.start();
//                boolean started = false;
                if (started) {
                    Runtime.getRuntime().addShutdownHook(new Thread(()->{
                        System.out.println("shutdown");
                        xrealHID.shutdown();
                    }));
                    // 开始追踪
                    started = xrealHID.startTrack();
                    if (!started) {
                        System.out.println("开启追踪失败，可能已经开启");
                    }
                    while (true) {
                        Quaternion quaternion = xrealHID.rotationUpdater.getRotation();
                        System.out.println(xrealHID.report.toString());
                        System.out.println(quaternion);
//                        {
                            // 使用四元数的分量创建Rotation实例
                            Rotation rotation = new Rotation(quaternion.getQ0(), quaternion.getQ1(), quaternion.getQ2(), quaternion.getQ3(), true);
                            // 提取欧拉角
                            double[] eulerAngles = rotation.getAngles(RotationOrder.XYZ);
                            double roll = Math.round(Math.toDegrees(eulerAngles[0]) * 1000.0) / 1000.0;  // 绕X轴的旋转角度（度数）
                            double pitch = Math.round(Math.toDegrees(eulerAngles[1]) * 1000.0) / 1000.0; // 绕Y轴的旋转角度（度数）
                            double yaw = Math.round(Math.toDegrees(eulerAngles[2]) * 1000.0) / 1000.0;   // 绕Z轴的旋转角度（度数）
//                        }
                        textField1.setText("Counter: " + xrealHID.report.ticker);
                        textField2.setText("Counter: " + quaternion.toString());
                        textField3.setText("Counter: " + roll + ", " + pitch + ", " + yaw);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }).start();

    }
}

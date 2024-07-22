import java.util.Arrays;

public class Report {
    long ticker = 0;
    short[] ang_vel = new short[3];

    @Override
    public String toString() {
        return "Report{" +
            "ticker=" + ticker +
            ", ang_vel=" + Arrays.toString(ang_vel) +
            '}';
    }
}
package net.simforge.networkview;

public class TrackerUtil {
    public static final Object Minute = new Object();
    public static final Object Second = new Object();
    public static final Object Millisecond = new Object();

    public static double duration(double value, Object unit) {
        if (unit == Minute) {
            return value / 60;
        } else if (unit == Second) {
            return value / 60 / 60;
        } else if (unit == Millisecond) {
            return value / 60 / 60 / 1000;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static double duration(int hour, int minute, int second) {
        return hour + duration(minute, Minute) + duration(second, Second);
    }
}

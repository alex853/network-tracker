package net.simforge.networkview.datafeeder;

import java.time.LocalDateTime;

public class ReportUtils {
    private static final java.time.format.DateTimeFormatter timestampDateFormat_java = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static LocalDateTime fromTimestampJava(String timestamp) {
        return LocalDateTime.parse(timestamp, timestampDateFormat_java);
    }

    public static String toTimestamp(LocalDateTime dt) {
        return timestampDateFormat_java.format(dt);
    }

    public static boolean isTimestampGreater(String timestamp1, String timestamp2) {
        if (!isTimestamp(timestamp1)) {
            throw new IllegalArgumentException("Wrong timestamp provided: " + timestamp1);
        }
        if (!isTimestamp(timestamp2)) {
            throw new IllegalArgumentException("Wrong timestamp provided: " + timestamp2);
        }
        return timestamp1.compareTo(timestamp2) > 0;
    }

    public static boolean isTimestamp(String str) {
        return str.matches("\\d{14}");
    }
}

package net.simforge.tracker.tools;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.LocalDateTime;

public class ReportUtils {
    @Deprecated
    private static final DateTimeFormatter timestampDateFormat_joda = DateTimeFormat.forPattern("yyyyMMddHHmmss").withZoneUTC();
    private static final java.time.format.DateTimeFormatter timestampDateFormat_java = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Use {@link #fromTimestampJava(String)} instead
     */
    @Deprecated
    public static DateTime fromTimestamp(String timestamp) {
        return timestampDateFormat_joda.parseDateTime(timestamp);
    }

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

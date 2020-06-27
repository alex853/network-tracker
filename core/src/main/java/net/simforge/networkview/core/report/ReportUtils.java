package net.simforge.networkview.core.report;

import net.simforge.commons.misc.JavaTime;

import java.time.LocalDateTime;

public class ReportUtils {
    private static final java.time.format.DateTimeFormatter timestampDateFormat_java = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static LocalDateTime fromTimestampJava(String timestamp) {
        return LocalDateTime.parse(timestamp, timestampDateFormat_java);
    }

    public static String toTimestamp(LocalDateTime dt) {
        return timestampDateFormat_java.format(dt);
    }

    public static boolean isTimestampGreater(String greaterTimestamp, String anotherTimestamp) {
        assertTimestamp(greaterTimestamp);
        assertTimestamp(anotherTimestamp);
        return greaterTimestamp.compareTo(anotherTimestamp) > 0;
    }

    public static boolean isTimestampGreaterOrEqual(String greaterTimestamp, String anotherTimestamp) {
        assertTimestamp(greaterTimestamp);
        assertTimestamp(anotherTimestamp);
        return greaterTimestamp.compareTo(anotherTimestamp) >= 0;
    }

    public static boolean isTimestampLess(String lowerTimestamp, String anotherTimestamp) {
        assertTimestamp(lowerTimestamp);
        assertTimestamp(anotherTimestamp);
        return lowerTimestamp.compareTo(anotherTimestamp) < 0;
    }

    public static boolean isTimestampLessOrEqual(String lowerTimestamp, String anotherTimestamp) {
        assertTimestamp(lowerTimestamp);
        assertTimestamp(anotherTimestamp);
        return lowerTimestamp.compareTo(anotherTimestamp) <= 0;
    }

    private static void assertTimestamp(String timestamp) {
        if (!isTimestamp(timestamp)) {
            throw new IllegalArgumentException("Wrong timestamp provided: " + timestamp);
        }
    }

    public static boolean isTimestamp(String str) {
        return str.matches("\\d{14}");
    }

    public static String log(String report) {
        return "Report " + JavaTime.yMdHms.format(ReportUtils.fromTimestampJava(report));
    }

    public static String log(ReportInfo reportInfo) {
        return "Report " + JavaTime.yMdHms.format(reportInfo.getDt());
    }
}

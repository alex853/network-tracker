package net.simforge.networkview.tools;

import junit.framework.TestCase;
import net.simforge.commons.misc.JavaTime;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import java.time.LocalDateTime;

public class ReportUtilsTest extends TestCase {
    public void testIsTimestamp() throws Exception {
        assertTrue (ReportUtils.isTimestamp("20151201000000"));

        assertFalse(ReportUtils.isTimestamp("201512010000001"));
        assertFalse(ReportUtils.isTimestamp("20151201000000a"));
        assertFalse(ReportUtils.isTimestamp("20151201000"));
        assertFalse(ReportUtils.isTimestamp("aa20151201000"));
        assertFalse(ReportUtils.isTimestamp(""));
    }

    public void testIsTimestampGreater() throws Exception {
        assertTrue (ReportUtils.isTimestampGreater("20151202000000", "20151201000000")); // greater
        assertFalse(ReportUtils.isTimestampGreater("20151201000000", "20151201000000")); // same timestamp
        assertFalse(ReportUtils.isTimestampGreater("20151130000000", "20151201000000")); // less
    }

    public void testIsTimestampGreater_wrongArguments() throws Exception {
        try {
            assertTrue (ReportUtils.isTimestampGreater("20151201000000", "some_stupid_data"));
            fail();
        } catch (Exception e) {
            // correct control flow
        }

        try {
            assertTrue (ReportUtils.isTimestampGreater("some_stupid_data", "20151201000000"));
            fail();
        } catch (Exception e) {
            // correct control flow
        }
    }

    public void testFromTimestamp_joda() {
        DateTime dateTime = ReportUtils.fromTimestamp("20151201010203");
        assertEquals(2015, dateTime.getYear());
        assertEquals(12, dateTime.getMonthOfYear());
        assertEquals(1, dateTime.getDayOfMonth());
        assertEquals(1, dateTime.getHourOfDay());
        assertEquals(2, dateTime.getMinuteOfHour());
        assertEquals(3, dateTime.getSecondOfMinute());
    }

    public void testFromTimestamp_joda_wrongInput() {
        try {
            ReportUtils.fromTimestamp("2015a20a0102a3");
            fail();
        } catch (Exception e) {
            // correct control flow
        }
    }

    public void testUTCtimezone_joda() {
        DateTime dateTime = ReportUtils.fromTimestamp("20151201010203");
        assertEquals(DateTimeZone.UTC, dateTime.getZone());
    }

    public void testToTimestamp_java() {
        LocalDateTime dateTime = LocalDateTime.of(2015, 12, 1, 1, 2, 3);
        String timestamp = ReportUtils.toTimestamp(dateTime);
        assertEquals("20151201010203", timestamp);
    }

    public void testFromTimestamp_java() {
        LocalDateTime dateTime = ReportUtils.fromTimestampJava("20151201010203");
        assertEquals(2015, dateTime.getYear());
        assertEquals(12, dateTime.getMonthValue());
        assertEquals(1, dateTime.getDayOfMonth());
        assertEquals(1, dateTime.getHour());
        assertEquals(2, dateTime.getMinute());
        assertEquals(3, dateTime.getSecond());
    }

    public void testFromTimestamp_java_wrongInput() {
        try {
            ReportUtils.fromTimestampJava("2015a20a0102a3");
            fail();
        } catch (Exception e) {
            // correct control flow
        }
    }
}

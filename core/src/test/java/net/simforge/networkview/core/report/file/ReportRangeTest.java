package net.simforge.networkview.core.report.file;

import net.simforge.networkview.core.report.ReportInfo;
import net.simforge.networkview.core.report.ReportInfoDto;
import net.simforge.networkview.core.report.ReportRange;
import net.simforge.networkview.core.report.persistence.Report;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReportRangeTest {
    private static ReportInfo report1 = new ReportInfoDto(1L, "20010101000000");
    private static ReportInfo report2 = new ReportInfoDto(2L, "20020101000000");
    private static ReportInfo report3 = new ReportInfoDto(3L, "20030101000000");
    private static ReportInfo report4 = new ReportInfoDto(4L, "20040101000000");
    private static ReportInfo report5 = new ReportInfoDto(5L, "20050101000000");
    private static ReportInfo report6 = new ReportInfoDto(6L, "20060101000000");

    @Test
    public void between_goodRange_ok() {
        ReportRange range = ReportRange.between(report1, report4);
        assertNotNull(range);
        assertEquals(report1.getId(), range.getSince().getId());
        assertEquals(report4.getId(), range.getTill().getId());
    }

    @Test
    public void between_zeroLengthRange_ok() {
        ReportRange range = ReportRange.between(report1, report1);
        assertNotNull(range);
        assertEquals(report1.getId(), range.getSince().getId());
        assertEquals(report1.getId(), range.getTill().getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void between_invertedRange_exceptionThrown() {
        ReportRange.between(report4, report1);
    }

    @Test(expected = NullPointerException.class)
    public void between_nullSince_exceptionThrown() {
        //noinspection ConstantConditions
        ReportRange.between(null, report4);
    }

    @Test(expected = NullPointerException.class)
    public void between_nullTill_exceptionThrown() {
        //noinspection ConstantConditions
        ReportRange.between(report1, null);
    }

    @Test
    public void isWithin_within_true() {
        ReportRange range2to4 = ReportRange.between(report2, report4);
        assertTrue(range2to4.isWithin(report3));
    }

    @Test
    public void isWithin_sameAsSince_true() {
        ReportRange range2to4 = ReportRange.between(report2, report4);
        assertTrue(range2to4.isWithin(report2));
    }

    @Test
    public void isWithin_sameAsTill_true() {
        ReportRange range2to4 = ReportRange.between(report2, report4);
        assertTrue(range2to4.isWithin(report4));
    }

    @Test
    public void isWithin_beforeSince_false() {
        ReportRange range2to4 = ReportRange.between(report2, report4);
        assertFalse(range2to4.isWithin(report1));
    }

    @Test
    public void isWithin_afterTill_false() {
        ReportRange range2to4 = ReportRange.between(report2, report4);
        assertFalse(range2to4.isWithin(report5));
    }

    @Test(expected = NullPointerException.class)
    public void isWithin_null_exceptionThrown() {
        ReportRange range2to4 = ReportRange.between(report2, report4);
        //noinspection ConstantConditions
        range2to4.isWithin(null);
    }

    @Test
    public void intersect_sameRange_ok() {
        ReportRange range2to4 = ReportRange.between(report2, report4);
        ReportRange sameRange = ReportRange.between(report2, report4);
        ReportRange intersection = range2to4.intersect(sameRange);
        assertNotNull(intersection);
        assertEquals(report2.getId(), intersection.getSince().getId());
        assertEquals(report4.getId(), intersection.getTill().getId());
    }

    @Test
    public void intersect_noIntersection_null() {
        ReportRange range2to4 = ReportRange.between(report2, report4);
        ReportRange range5to6 = ReportRange.between(report5, report6);
        ReportRange intersection = range2to4.intersect(range5to6);
        assertNull(intersection);
    }

    @Test
    public void intersect_overlapSince_ok() {
        ReportRange range2to4 = ReportRange.between(report2, report4);
        ReportRange range1to3 = ReportRange.between(report1, report3);
        ReportRange intersection = range2to4.intersect(range1to3);
        assertNotNull(intersection);
        assertEquals(report2.getId(), intersection.getSince().getId());
        assertEquals(report3.getId(), intersection.getTill().getId());
    }

    @Test
    public void intersect_overlapTill_ok() {
        ReportRange range2to4 = ReportRange.between(report2, report4);
        ReportRange range3to5 = ReportRange.between(report3, report5);
        ReportRange intersection = range2to4.intersect(range3to5);
        assertNotNull(intersection);
        assertEquals(report3.getId(), intersection.getSince().getId());
        assertEquals(report4.getId(), intersection.getTill().getId());
    }

    @Test
    public void intersect_overlapWholeRange_ok() {
        ReportRange range2to4 = ReportRange.between(report2, report4);
        ReportRange range1to5 = ReportRange.between(report1, report5);
        ReportRange intersection = range2to4.intersect(range1to5);
        assertNotNull(intersection);
        assertEquals(report2.getId(), intersection.getSince().getId());
        assertEquals(report4.getId(), intersection.getTill().getId());
    }

    @Test
    public void intersect_zeroLengthIntersection_zeroLengthRange() {
        ReportRange range2to4 = ReportRange.between(report2, report4);
        ReportRange range3to3 = ReportRange.between(report3, report3);
        ReportRange intersection = range2to4.intersect(range3to3);
        assertNotNull(intersection);
        assertEquals(report3.getId(), intersection.getSince().getId());
        assertEquals(report3.getId(), intersection.getTill().getId());
    }

    @Test(expected = NullPointerException.class)
    public void intersect_null_exceptionThrown() {
        ReportRange range2to4 = ReportRange.between(report2, report4);
        //noinspection ConstantConditions
        range2to4.intersect(null);
    }

}

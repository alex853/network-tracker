package net.simforge.networkview.flights.model;

import junit.framework.TestCase;
import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.TrackerUtil;
import net.simforge.networkview.flights.datasource.CsvDatasource;

import java.io.InputStream;

public class TrackingUtilTest extends TestCase {

    public void testGetTimeBetween() throws Exception {
        InputStream is = Class.class.getResourceAsStream("/snapshots/pilot-1309680_from-1_amount-60.csv");
        String csvContent = IOHelper.readInputStream(is);
        MainContext mainContext = new MainContext();
        mainContext.setReportDatasource(new CsvDatasource(Csv.fromContent(csvContent)));

        assertEquals(TrackerUtil.duration(0, 2, 1), mainContext.getTimeBetween(1, 2));
        assertEquals(TrackerUtil.duration(0, 4, 4), mainContext.getTimeBetween(1, 3));
        assertEquals(TrackerUtil.duration(0, 6, 5), mainContext.getTimeBetween(1, 4));
    }

    public void testDuration() throws Exception {
        assertEquals(0.5, TrackerUtil.duration(30, TrackerUtil.Minute));
        assertEquals(0.25, TrackerUtil.duration(900, TrackerUtil.Second));
        assertEquals(1.0 / 3600, TrackerUtil.duration(1000, TrackerUtil.Millisecond));
    }

    public void testDurationHMS() throws Exception {
        assertEquals(1.0 + 2.0/60 + 3./60/60, TrackerUtil.duration(1, 2, 3));
    }
}

package net.simforge.networkview.flights.method.eventbased;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class Test_Pilot913904_2020_05_02_TouchAndGoInEGTB extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-913904_2020-05-02-touch-and-goes-in-egtb.csv");
        initNoOpPersistence();
        doTest(913904, 1006960, 1007000);
    }

    public void report_1006989() {
        checkTouchAndGoEvent();
    }

    // Went Offline, check Finished flight
    public void report_1006999() {
        Flight finishedFlight = getFlightFromStatusEvent(FlightStatus.Finished);
        assertNotNull(finishedFlight);
        checkFlightRoute(finishedFlight, "EGTB", "EGTB");

        checkFlightTimeMins(finishedFlight, 19);

        checkOfflineEvent();
    }
}

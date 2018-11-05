package net.simforge.networkview.flights3;

import net.simforge.networkview.flights2.flight.FlightStatus;
import org.junit.Test;

import java.io.IOException;

/**
 * Pilot connected to network in air. No any previous suitable flight is found.
 *
 * Expected behaviour:
 *   1) Flight started, firstSeen and lastSeen are set, departure and destination are empty.
 */
public class Case_InAirConnected extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-1261420_from-670000_amount-10000.csv");
        initNoOpPersistence();
        doTest(1261420, 670537, 670543);
    }

    public void report_670538() {
        checkNoFlight();
    }

    public void report_670539() {
        checkOnlineEvent();
        checkFlight();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute(null, null);
    }
}

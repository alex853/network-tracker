package net.simforge.networkview.flights3;

import net.simforge.networkview.flights2.flight.FlightStatus;
import org.junit.Test;

import java.io.IOException;

/**
 * Pilot connected in one airport for few mins. Then disconnected and reconnected in another airport.
 *
 * Expected behaviour:
 *   1) Flight for first connection terminated.
 *   2) Flight for second connection started as it determines on-ground jump.
 */
public class Case_JumpOnGround_Offline extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-1261420_from-670000_amount-10000.csv");
        initNoOpPersistence();
        doTest(1261420, 670700, 670720);
    }

    public void report_670700_670701() {
        checkNoPilotContext();
    }

    public void report_670702_670704() {
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightLastSeenIcao("UKBB");
        checkFlightRoute(null, null);
        checkRecentFlightCount(0);
    }

    public void report_670705() {
        checkOfflineEvent();
        checkNoFlight();
        checkRecentFlightCount(1);
    }

    public void report_670706() {
        checkOnlineEvent();
    }

    public void report_670706_670716() {
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightLastSeenIcao("UUDD");
        checkFlightRoute(null, null);
        checkRecentFlightCount(1);
    }

    public void report_670717() {
        checkOfflineEvent();
    }

    public void report_670717_670720() {
        checkNoFlight();
        checkRecentFlightCount(2);
    }
}

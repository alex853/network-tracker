package net.simforge.networkview.flights3;

import net.simforge.networkview.flights2.flight.FlightStatus;
import org.junit.Test;

import java.io.IOException;

/**
 * Pilot changes aircraft position from one airport to another while he is connected to network.
 * It looks like instant jump "on ground".
 *
 * Expected behaviour:
 *   1) Finish or terminate the first flight.
 *   2) Start the second flight.
 */
public class Case_JumpOnGround extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-1261420_from-670000_amount-10000.csv");
        initNoOpPersistence();
        doTest(1261420, 675005, 675015);
    }

    public void report_675005_675008() {
        checkFlightRoute(null, null);
        checkFlightLastSeenIcao("UUDD");
    }

    public void report_675009() {
        Flight terminatedFlight = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightRoute(terminatedFlight, null, null);
        checkFlightLastSeenIcao(terminatedFlight, "UUDD");

        Flight nextFlight = getFlightFromStatusEvent(FlightStatus.Preparing);
        checkFlightRoute(nextFlight, null, null);
        checkFlightLastSeenIcao(nextFlight, "UWWW");
    }
}

package net.simforge.networkview.flights2;

import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.flight.FlightStatus;
import org.junit.Test;

import java.io.IOException;

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

        Flight nextFlight = getFlightFromStatusEvent(FlightStatus.Departure);
        checkFlightRoute(nextFlight, null, null);
        checkFlightLastSeenIcao(nextFlight, "UWWW");
    }
}

package net.simforge.networkview.flights2;

import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.flight.FlightStatus;
import org.junit.Test;

import java.io.IOException;

/**
 * Pilot was cruising previous flight and then disappeared. After that he reconnected in few mins in absolutely
 * different point of the world. It seems FS crashed and pilot decided to start a different flight.
 *
 * Expected behaviour:
 *   1) The first flight is finished.
 *   2) The second "on ground" flight is started.
 */
public class Case_HugeJumpAfterFSCrash extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-1261420_from-670000_amount-10000.csv");
        initNoOpPersistence();
        doTest(1261420, 670550, 670577);
    }

    // Pilot is cruising
    public void report_670550_670538() {
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute(null, null); // flight route is null-null because the previous flight is not fully tracked in this scenario
    }

    // Pilot disappears
    public void report_670571() {
        checkFlightStatus(FlightStatus.Lost);
        checkFlightRoute(null, null); // flight route is null-null because the previous flight is not fully tracked in this scenario
    }

    // Reconnected on ground in different airport
    public void report_670573() {
        Flight terminatedFlight = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightRoute(terminatedFlight, null, null);

        checkOnlineEvent();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightRoute("ULLI", null);
        checkFlightplanEvent();
        checkFlightplanData("T154", "EPWA", "LEAM");
    }

}

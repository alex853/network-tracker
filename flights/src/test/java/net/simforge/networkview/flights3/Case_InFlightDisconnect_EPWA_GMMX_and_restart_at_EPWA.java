package net.simforge.networkview.flights3;

import net.simforge.networkview.flights2.flight.FlightStatus;
import org.junit.Test;

import java.io.IOException;

/**
 * Pilot started flight EPWA-GMMX, took off from EPWA and then disconnected shortly after that.
 * Pilot reconnected back to network half an hour back in EPWA and started new flight EPWA-GMMX.
 *
 * Excepcted behaviour:
 *   1) The first flight is terminated.
 *   2) The second flight is started.
 */
public class Case_InFlightDisconnect_EPWA_GMMX_and_restart_at_EPWA extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-1261420_from-670000_amount-10000.csv");
        initNoOpPersistence();
        doTest(1261420, 670462, 670495);
    }

    public void report_670462_670473() {
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightLastSeenIcao("EPWA");
        checkFlightRoute(null, null);
    }

    public void report_670474() {
        checkFlight();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EPWA", null);
        checkFlightplanData("T154", "EPWA", "GMMX");
    }

    public void report_670475_670490() {
        checkFlightStatus(FlightStatus.Lost);
        checkFlightRoute("EPWA", null);
    }

    public void report_670491() {
        checkRecentFlightCount(1);
        Flight terminatedFlight = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightRoute(terminatedFlight, "EPWA", null);

        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightLastSeenIcao("EPWA");
        checkFlightRoute(null, null);
        checkFlightplanData("T154", "EPWA", "GMMX");
    }
}

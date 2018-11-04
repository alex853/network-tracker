package net.simforge.networkview.flights2;

import net.simforge.networkview.flights2.flight.FlightStatus;
import org.junit.Test;

import java.io.IOException;

public class Test_Pilot1045435_RJAAtoVHHH extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-1045435_from-2015179142_amount-250.csv");
        initNoOpPersistence();
        doTest(1045435, 2015179142, 2015179392);
    }

    public void report_2015179165() {
        checkOnlineEvent();
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightLastSeenIcao("RJAA");
        checkFlightRoute(null, null);
        checkFlightplanData("B77W", "RJAA", "VHHH");
    }

    // this failed without corrected Circle boundary for RJAA
    public void report_2015179169() {
        checkFlightStatus(FlightStatus.Departure);
        checkFlightLastSeenIcao("RJAA");
        checkFlightRoute(null, null);
    }

    public void report_2015179170() {
        checkTakeoffEvent();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("RJAA", null);
    }

    public void report_2015179289() {
        checkLandingEvent();
        checkFlightStatus(FlightStatus.Arrival);
        checkFlightRoute("RJAA", "VHHH");
    }

    public void report_2015179293() {
        checkOfflineEvent();
        checkNoFlight();
    }
}
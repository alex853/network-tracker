package net.simforge.networkview.flights3;

import net.simforge.networkview.flights2.flight.FlightStatus;
import org.junit.Test;

import java.io.IOException;

public class Case_NormalFlight_ULLI_to_USPP extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-1300812_from-1_amount-1500.csv");
        initNoOpPersistence();
        doTest(1300812, 370, 440);
    }

    public void report_374() {
        checkOnlineEvent();
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightLastSeenIcao("ULLI");
        checkFlightRoute(null, null);
    }

    public void report_375() {
        checkFlightplanEvent();
        checkFlightplanData("A320", "ULLI", "USPP");
    }

    public void report_380() {
        checkTakeoffEvent();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightLastSeenIcao(null);
        checkFlightRoute("ULLI", null);
    }

    public void report_433() {
        checkLandingEvent();
        checkFlightStatus(FlightStatus.Arrival);
        checkFlightRoute("ULLI", "USPP");
    }

    public void report_435() {
        checkOfflineEvent();
        checkNoFlight();
    }
}
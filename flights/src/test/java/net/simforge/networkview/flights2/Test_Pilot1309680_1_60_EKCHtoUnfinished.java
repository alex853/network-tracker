package net.simforge.networkview.flights2;

import net.simforge.networkview.flights2.flight.FlightStatus;
import org.junit.Test;

import java.io.IOException;

public class Test_Pilot1309680_1_60_EKCHtoUnfinished extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-1309680_from-1_amount-60.csv");
        initNoOpPersistence();
        doTest(1309680, 1, 60);
    }

    public void report_1_6() {
        checkNoPilotContext();
    }

    public void report_7() {
        checkOnlineEvent();
        checkOnGround();
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightLastSeenIcao("EKCH");
        checkFlightRoute(null, null);
    }

    public void report_8_18() {
        checkOnGround();
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightLastSeenIcao("EKCH");
        checkFlightRoute(null, null);
    }

    public void report_19() {
        checkFlying();
        checkTakeoffEvent();
        checkFlight();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EKCH", null);
    }

    public void report_20_37() {
        checkFlying();
        checkFlight();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EKCH", null);
        checkFlightCallsign("DAL21");
    }

    public void report_38() {
        checkOfflineEvent();
        checkPositionUnknown();
        checkFlight();
        checkFlightStatus(FlightStatus.Lost);
    }

    public void report_39_60() {
        checkPositionUnknown();
        checkFlight();
        checkFlightStatus(FlightStatus.Lost);
    }
}
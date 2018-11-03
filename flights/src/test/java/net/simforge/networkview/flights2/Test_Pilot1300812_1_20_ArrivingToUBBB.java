package net.simforge.networkview.flights2;

import org.junit.Test;

import java.io.IOException;

public class Test_Pilot1300812_1_20_ArrivingToUBBB extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-1300812_from-1_amount-60.csv");
        initNoOpPersistence();
        doTest(1300812, 1, 20);
    }

    public void report_1() {
        checkPositionKnown();
        checkOnlineEvent();
        checkFlying();
        checkFlight();
        checkFlightRoute(null, null);
    }

    public void report_2_13() {
        checkPositionKnown();
        checkFlight();
    }

    public void report_14() {
        checkPositionKnown();
        checkOnGround();
        checkLandingEvent();
        checkFlight();
        checkFlightRoute(null, "UBBB");
    }

    public void report_15_16() {
        checkPositionKnown();
        checkOnGround();
        checkFlight();
        checkFlightRoute(null, "UBBB");
    }

    public void report_17() {
        checkPositionUnknown();
        checkOfflineEvent();
        checkNoFlight();
    }

    public void report_18_22() {
        checkPositionUnknown();
        checkNoFlight();
    }
}

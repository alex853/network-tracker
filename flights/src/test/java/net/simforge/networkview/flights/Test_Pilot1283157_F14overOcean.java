package net.simforge.networkview.flights;

import org.junit.Test;

import java.io.IOException;

public class Test_Pilot1283157_F14overOcean extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-1283157_from-2015182747_amount-250.csv");
        initNoOpPersistence();
        doTest(1283157, 2015182747, 2015182997);
    }

    public void report_2015182767() {
        checkOnlineEvent();
        checkOnGround();

        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightRoute(null, null);
    }

    public void report_2015182770() {
        checkFlightStatus(FlightStatus.Departure);
        checkFlightRoute(null, null);
    }

    public void report_2015182771() {
        checkTakeoffEvent();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute(ON_GROUND, null);
    }

    public void report_2015182777() {
        checkLandingEvent();
        checkFlightStatus(FlightStatus.Arrival);
        checkFlightRoute(ON_GROUND, ON_GROUND);
    }

    public void report_2015182783() {
        checkFlightStatusEvent(FlightStatus.Finished);

        checkFlightStatus(FlightStatus.Departure);
        checkFlightplanData("F14", "CV69", "CV69");
    }

    public void report_2015182784() {
        checkTakeoffEvent();
    }

    public void report_2015182788() {
        checkLandingEvent();
    }

    public void report_2015182793() {
        checkFlightStatusEvent(FlightStatus.Finished);

        checkFlightStatusEvent(FlightStatus.Departure);
        checkTakeoffEvent();
        checkFlightStatusEvent(FlightStatus.Flying);
        checkFlightStatus(FlightStatus.Flying);
        checkFlightplanData("F14", "CV69", "CV69");
    }

    public void report_2015182808() {
        checkLandingEvent();
    }

    public void report_2015182813() {
        checkFlightStatusEvent(FlightStatus.Finished);

        checkFlightStatus(FlightStatus.Departure);
        checkFlightplanData("F14", "CV69", "CV69");
    }

    public void report_2015182815() {
        checkTakeoffEvent();
    }

    public void report_2015182821() {
        checkLandingEvent();
    }

    public void report_2015182826() {
        checkFlightStatusEvent(FlightStatus.Finished);

        checkFlightStatus(FlightStatus.Departure);
        checkFlightplanData("F14", "CV69", "CV69");
    }

    public void report_2015182835() {
        checkTakeoffEvent();
    }

    public void report_2015182841() {
        checkLandingEvent();
    }

    public void report_2015182847() {
        checkFlightStatusEvent(FlightStatus.Finished);

        checkFlightStatus(FlightStatus.Departure);
        checkFlightplanData("F14", "CV69", "CV69");
    }

    public void report_2015182856() {
        checkOfflineEvent();
        checkFlightStatusEvent(FlightStatus.Terminated);
    }
}
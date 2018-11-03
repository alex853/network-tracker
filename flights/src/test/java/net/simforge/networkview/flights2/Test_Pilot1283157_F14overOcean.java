package net.simforge.networkview.flights2;

import net.simforge.networkview.flights2.flight.FlightStatus;
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

        checkMovement();
        checkMovementStatus(FlightStatus.Departure);
        checkMovementRoute(null, null);
    }

    public void report_2015182770() {
        checkMovementStatus(FlightStatus.Departure);
        checkMovementRoute(null, null);
    }

    public void report_2015182771() {
        checkTakeoffEvent();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute(null, null);
    }

    public void report_2015182777() {
        checkLandingEvent();
        checkMovementStatus(FlightStatus.Arrival);
        checkMovementRoute(null, null);
    }

    public void report_2015182783() {
        checkMovementStatusEvent(FlightStatus.Finished);

        checkMovementStatus(FlightStatus.Departure);
        checkMovementFlightplanData("F14", "CV69", "CV69");
    }

    public void report_2015182784() {
        checkTakeoffEvent();
    }

    public void report_2015182788() {
        checkLandingEvent();
    }

    public void report_2015182793() {
        checkMovementStatusEvent(FlightStatus.Finished);

        checkMovementStatusEvent(FlightStatus.Departure);
        checkTakeoffEvent();
        checkMovementStatusEvent(FlightStatus.Flying);
        checkMovementStatus(FlightStatus.Flying);
        checkMovementFlightplanData("F14", "CV69", "CV69");
    }

    public void report_2015182808() {
        checkLandingEvent();
    }

    public void report_2015182813() {
        checkMovementStatusEvent(FlightStatus.Finished);

        checkMovementStatus(FlightStatus.Departure);
        checkMovementFlightplanData("F14", "CV69", "CV69");
    }

    public void report_2015182815() {
        checkTakeoffEvent();
    }

    public void report_2015182821() {
        checkLandingEvent();
    }

    public void report_2015182826() {
        checkMovementStatusEvent(FlightStatus.Finished);

        checkMovementStatus(FlightStatus.Departure);
        checkMovementFlightplanData("F14", "CV69", "CV69");
    }

    public void report_2015182835() {
        checkTakeoffEvent();
    }

    public void report_2015182841() {
        checkLandingEvent();
    }

    public void report_2015182847() {
        checkMovementStatusEvent(FlightStatus.Finished);

        checkMovementStatus(FlightStatus.Departure);
        checkMovementFlightplanData("F14", "CV69", "CV69");
    }

    public void report_2015182856() {
        checkOfflineEvent();
        checkMovementStatusEvent(FlightStatus.Terminated);
    }
}
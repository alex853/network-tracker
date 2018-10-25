package net.simforge.tracker.flights.model;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.tracker.flights.datasource.CsvDatasource;

import java.io.InputStream;

public class Test_Pilot1283157_F14overOcean extends TrackingTest {
    @Override
    protected void setUp() throws Exception {
        InputStream is = Class.class.getResourceAsStream("/net/simforge/tracker/flights/model/pilot-1283157_from-2015182747_amount-250.csv");
        String csvContent = IOHelper.readInputStream(is);

        setDatasource(new CsvDatasource(Csv.fromContent(csvContent)));
        init(1283157, 2015182747, 2015182997);
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
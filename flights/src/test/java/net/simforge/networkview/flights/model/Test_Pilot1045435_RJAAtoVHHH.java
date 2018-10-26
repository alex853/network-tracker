package net.simforge.networkview.flights.model;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.flights.datasource.CsvDatasource;

import java.io.InputStream;

public class Test_Pilot1045435_RJAAtoVHHH extends TrackingTest {
    @Override
    protected void setUp() throws Exception {
        InputStream is = Class.class.getResourceAsStream("/snapshots/pilot-1045435_from-2015179142_amount-250.csv");
        String csvContent = IOHelper.readInputStream(is);

        setDatasource(new CsvDatasource(Csv.fromContent(csvContent)));
        init(1045435, 2015179142, 2015179392);
    }

    public void report_2015179165() {
        checkOnlineEvent();
        checkMovement();
        checkMovementStatus(FlightStatus.Departure);
        checkMovementRoute("RJAA", null);
        checkMovementFlightplanData("H/B77W", "RJAA", "VHHH");
    }

    // this failed without corrected Circle boundary for RJAA
    public void report_2015179169() {
        checkMovementStatus(FlightStatus.Departure);
        checkMovementRoute("RJAA", null);
    }

    public void report_2015179170() {
        checkTakeoffEvent();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("RJAA", null);
    }

    public void report_2015179289() {
        checkLandingEvent();
        checkMovementStatus(FlightStatus.Arrival);
        checkMovementRoute("RJAA", "VHHH");
    }

    public void report_2015179293() {
        checkOfflineEvent();
        checkNoMovement();
    }
}
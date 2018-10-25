package net.simforge.tracker.flights.model;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.tracker.flights.datasource.CsvDatasource;

import java.io.InputStream;

public class Test_Pilot1300812_370_440_ULLItoUSPP extends TrackingTest {
    @Override
    protected void setUp() throws Exception {
        InputStream is = Class.class.getResourceAsStream("/net/simforge/tracker/flights/model/pilot-1300812_from-1_amount-1500.csv");
        String csvContent = IOHelper.readInputStream(is);

        setDatasource(new CsvDatasource(Csv.fromContent(csvContent)));
        init(1300812, 370, 440);
    }

    public void report_374() {
        checkOnlineEvent();
        checkMovement();
        checkMovementStatus(FlightStatus.Departure);
        checkMovementRoute("ULLI", null);
    }

    public void report_375() {
        checkMovementFlightplanEvent();
        checkMovementFlightplanData("A320/G", "ULLI", "USPP");
    }

    public void report_380() {
        checkTakeoffEvent();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("ULLI", null);
    }

    public void report_433() {
        checkLandingEvent();
        checkMovementStatus(FlightStatus.Arrival);
        checkMovementRoute("ULLI", "USPP");
    }

    public void report_435() {
        checkOfflineEvent();
        checkNoMovement();
    }
}
package net.simforge.networkview.flights2;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.flights.datasource.CsvDatasource;
import net.simforge.networkview.flights2.flight.FlightStatus;

import java.io.InputStream;

public class Test_Pilot1300812_370_440_ULLItoUSPP extends TrackingTest {
    @Override
    protected void setUp() throws Exception {
        InputStream is = Class.class.getResourceAsStream("/snapshots/pilot-1300812_from-1_amount-1500.csv");
        String csvContent = IOHelper.readInputStream(is);

        setDatasource(new CsvDatasource(Csv.fromContent(csvContent)));
        init(1300812, 370, 440);
    }

    public void report_374() {
        checkOnlineEvent();
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightRoute("ULLI", null);
    }

    public void report_375() {
        checkFlightFlightplanEvent();
        checkFlightFlightplanData("A320", "ULLI", "USPP");
    }

    public void report_380() {
        checkTakeoffEvent();
        checkFlightStatus(FlightStatus.Flying);
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
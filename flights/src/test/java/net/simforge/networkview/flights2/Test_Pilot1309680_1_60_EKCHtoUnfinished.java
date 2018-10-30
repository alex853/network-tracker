package net.simforge.networkview.flights2;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.flights.datasource.CsvDatasource;
import net.simforge.networkview.flights2.flight.FlightStatus;

import java.io.InputStream;

public class Test_Pilot1309680_1_60_EKCHtoUnfinished extends TrackingTest {
    @Override
    protected void setUp() throws Exception {
        InputStream is = Class.class.getResourceAsStream("/snapshots/pilot-1309680_from-1_amount-60.csv");
        String csvContent = IOHelper.readInputStream(is);

        setDatasource(new CsvDatasource(Csv.fromContent(csvContent)));
        init(1309680, 1, 60);
    }

    public void report_1_6() {
        checkNoPilotContext();
    }

    public void report_7() {
        checkOnlineEvent();
        checkOnGround();
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightRoute("EKCH", null);
    }

    public void report_8_18() {
        checkOnGround();
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightRoute("EKCH", null);
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
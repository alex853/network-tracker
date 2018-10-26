package net.simforge.networkview.flights.model;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.flights.datasource.CsvDatasource;

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
        checkPositionUnknown();
        checkNoMovement();
    }

    public void report_7() {
        checkOnlineEvent();
        checkOnGround();
        checkMovement();
        checkMovementStatus(FlightStatus.Departure);
        checkMovementRoute("EKCH", null);
    }

    public void report_8_18() {
        checkOnGround();
        checkMovement();
        checkMovementStatus(FlightStatus.Departure);
        checkMovementRoute("EKCH", null);
    }

    public void report_19() {
        checkFlying();
        checkTakeoffEvent();
        checkMovement();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("EKCH", null);
    }

    public void report_20_37() {
        checkFlying();
        checkMovement();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("EKCH", null);
    }

    public void report_38() {
        checkOfflineEvent();
        checkPositionUnknown();
        checkMovement();
        checkMovementStatus(FlightStatus.Lost);
    }

    public void report_39_60() {
        checkPositionUnknown();
        checkMovement();
        checkMovementStatus(FlightStatus.Lost);
    }
}
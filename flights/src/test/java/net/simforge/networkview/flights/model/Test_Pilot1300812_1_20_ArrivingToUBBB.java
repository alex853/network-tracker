package net.simforge.networkview.flights.model;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.flights.datasource.CsvDatasource;

import java.io.InputStream;

public class Test_Pilot1300812_1_20_ArrivingToUBBB extends TrackingTest {
    @Override
    protected void setUp() throws Exception {
        InputStream is = Class.class.getResourceAsStream("/net/simforge/networkview/flights/model/pilot-1300812_from-1_amount-60.csv");
        String csvContent = IOHelper.readInputStream(is);

        setDatasource(new CsvDatasource(Csv.fromContent(csvContent)));
        init(1300812, 1, 20);
    }

    public void report_1() {
        checkPositionKnown();
        checkOnlineEvent();
        checkFlying();
        checkMovement();
        checkMovementRoute(null, null);
    }

    public void report_2_13() {
        checkPositionKnown();
        checkMovement();
    }

    public void report_14() {
        checkPositionKnown();
        checkOnGround();
        checkLandingEvent();
        checkMovement();
        checkMovementRoute(null, "UBBB");
    }

    public void report_15_16() {
        checkPositionKnown();
        checkOnGround();
        checkMovement();
        checkMovementRoute(null, "UBBB");
    }

    public void report_17() {
        checkPositionUnknown();
        checkOfflineEvent();
        checkNoMovement();
    }

    public void report_18_22() {
        checkPositionUnknown();
        checkNoMovement();
    }
}

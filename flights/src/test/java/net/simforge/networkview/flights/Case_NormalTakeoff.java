package net.simforge.networkview.flights;

import org.junit.Test;

import java.io.IOException;

/**
 * Pilot connected to network on ground and performed normal takeoff.
 *
 * Expected behaviour:
 *   1) Flight started, firstSeen and lastSeen are set, departure is set, destination is empty.
 */
public class Case_NormalTakeoff extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-1261420_from-670000_amount-10000.csv");
        initNoOpPersistence();
        doTest(1261420, 670485, 670500);
    }

    public void report_670485_670490() {
        checkNoFlight();
    }

    public void report_670491() {
        checkOnlineEvent();
    }

    public void report_670491_670496() {
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightRoute(null, null);
    }

    public void report_670497() {
        checkTakeoffEvent();
    }

    public void report_670497_670500() {
        checkFlight();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EPWA", null);
    }
}

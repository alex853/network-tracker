package net.simforge.networkview.flights.method.eventbased;

import org.junit.Test;

import java.io.IOException;

/**
 * The pilot started a flight. Some disconnections happened during the flight (1-2 missing reports).
 *
 * Expected behaviour:
 *   1) Flight resumes after each disconnection.
 */
public class Case_InFlightDisconnect_Ellipse_ShortDisconnectsDuringNormalFlight extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-1261420_from-670000_amount-10000.csv");
        initNoOpPersistence();
        doTest(1261420, 670490, 670571);
    }

    // 670491 EPWA-.... Non-finished flight
    public void report_670491() {
        checkOnlineEvent();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightRoute(null, null);
        checkFlightLastSeenIcao("EPWA");
        checkFlightplanEvent();
        checkFlightplanData("T154", "EPWA", "GMMX");
    }

    public void report_670493() {
        checkFlightStatus(FlightStatus.Departure);
        checkFlightRoute(null, null);
        checkFlightLastSeenIcao("EPWA");
        checkFlightplanEvent();
        checkFlightplanData("T154", "EPWA", "LEAM");
    }

    public void report_670497() {
        checkTakeoffEvent();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EPWA", null);
    }

    // Short disconnect at 670518
    public void report_670519() {
        checkRecentFlightCount(0);
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EPWA", null);
    }

    // Short disconnect at 670523
    public void report_670524() {
        checkRecentFlightCount(0);
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EPWA", null);
    }

    // Short disconnect at 670530
    public void report_670531() {
        checkRecentFlightCount(0);
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EPWA", null);
    }

    // Short disconnect at 670537-670538
    public void report_670539() {
        checkRecentFlightCount(0);
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EPWA", null);
    }

    public void report_670571() {
        checkRecentFlightCount(0);
        checkFlightStatus(FlightStatus.Lost);
        checkFlightRoute("EPWA", null);
    }
}

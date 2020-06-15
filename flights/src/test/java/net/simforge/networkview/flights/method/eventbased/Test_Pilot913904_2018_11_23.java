package net.simforge.networkview.flights.method.eventbased;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class Test_Pilot913904_2018_11_23 extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-913904_2018-11-23.csv");
        initNoOpPersistence();
        doTest(913904, 1016600, 1016849);
    }

    public void report_1016850_1016851() {
        checkNoPilotContext();
    }

    // EGKK-EGJJ
    // Departure section
    public void report_1016602_1016620() {
        checkOnGround();
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightLastSeenIcao("EGKK");
        checkFlightRoute(null, null);
    }

    // Online event
    public void report_1016602() {
        checkOnlineEvent();
        checkFlightStatus(FlightStatus.Preparing);
    }

    // Flightplan sent
    public void report_1016606() {
        checkFlightplanEvent();
        checkFlightplanData("A319", "EGKK", "EGJJ");
    }

    // Taxiing out
    public void report_1016617_1016620() {
        checkFlightStatus(FlightStatus.Departing);
    }

    // Takeoff
    public void report_1016621() {
        checkTakeoffEvent();
    }

    // Flying section
    public void report_1016621_1016644() {
        checkFlying();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EGKK", null);
        checkFlightplanData("A319", "EGKK", "EGJJ");
    }

    // Landing event
    public void report_1016645() {
        checkLandingEvent();
        checkFlightStatus(FlightStatus.TouchedDown);
    }

    // Arrival section
    public void report_1016645_1016651() {
        checkOnGround();
        checkFlight();
        checkFlightStatus(FlightStatus.Arrival);
        checkFlightLastSeenIcao("EGJJ");
        checkFlightRoute("EGKK", "EGJJ");
    }

    // Arrived event
    public void report_1016651() {
        checkFlightStatus(FlightStatus.Arrived);
    }

    // EGJJ-EGKK
    // New flight started
    public void report_1016652() {
        Flight finishedFlight = getFlightFromStatusEvent(FlightStatus.Finished);
        assertNotNull(finishedFlight);
        checkFlightRoute(finishedFlight, "EGKK", "EGJJ");

        checkCallsign("BAW160J");
        checkFlightStatus(FlightStatus.Preparing);
        checkFlightLastSeenIcao("EGJJ");
        checkFlightRoute(null, null);
    }

    // Departure section
    public void report_1016652_1016665() {
        checkOnGround();
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightLastSeenIcao("EGJJ");
        checkFlightRoute(null, null);
        checkFlightplanData("A319", "EGJJ", "EGKK");
    }

    // Taxiing out
    public void report_1016663_1016665() {
        checkFlightStatus(FlightStatus.Departing);
    }

    // Takeoff
    public void report_1016666() {
        checkTakeoffEvent();
    }

    // Flying section
    public void report_1016666_1016684() {
        checkFlying();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EGJJ", null);
        checkFlightplanData("A319", "EGJJ", "EGKK");
    }

    // Landing event
    public void report_1016685() {
        checkLandingEvent();
        checkFlightStatus(FlightStatus.TouchedDown);
    }

    // Arrival section
    public void report_1016685_1016688() {
        checkOnGround();
        checkFlight();
        checkFlightStatus(FlightStatus.Arrival);
        checkFlightLastSeenIcao("EGKK");
        checkFlightRoute("EGJJ", "EGKK");
    }

    // No Arrived event

    // Went Offline, check Finished flight
    public void report_1016689() {
        Flight finishedFlight = getFlightFromStatusEvent(FlightStatus.Finished);
        assertNotNull(finishedFlight);
        checkFlightRoute(finishedFlight, "EGJJ", "EGKK");

        checkOfflineEvent();
    }

    // Was Offline few hours
    public void report_1016689_1016757() {
        checkNoPilotContextOrPositionUnknown();
    }

    // EGKK-EHAM
    // Departure section
    public void report_1016758_1016768() {
        checkOnGround();
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightLastSeenIcao("EGKK");
        checkFlightRoute(null, null);
    }

    // Online event
    public void report_1016758() {
        checkOnlineEvent();
        checkFlightStatus(FlightStatus.Preparing);
        checkCallsign("BAW78AM");
    }

    // Flightplan sent
    public void report_1016759() {
        checkFlightplanEvent();
        checkFlightplanData("A319", "EGKK", "EHAM");
    }

    // Taxiing out
    public void report_1016764_1016768() {
        checkFlightStatus(FlightStatus.Departing);
    }

    // Takeoff
    public void report_1016769() {
        checkTakeoffEvent();
    }

    // Flying section
    public void report_1016769_1016788() {
        checkFlying();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EGKK", null);
        checkFlightplanData("A319", "EGKK", "EHAM");
    }

    // Landing event
    public void report_1016789() {
        checkLandingEvent();
        checkFlightStatus(FlightStatus.TouchedDown);
    }

    // Arrival section
    public void report_1016789_1016796() {
        checkOnGround();
        checkFlight();
        checkFlightStatus(FlightStatus.Arrival);
        checkFlightLastSeenIcao("EHAM");
        checkFlightRoute("EGKK", "EHAM");
    }

    // Arrived event
    public void report_1016796() {
        checkFlightStatus(FlightStatus.Arrived);
    }


    // EHAM-EGKK - Flight was diverted back to EHAM due to navigation failure, flight plan was not updated
    // New flight started
    public void report_1016797() {
        Flight finishedFlight = getFlightFromStatusEvent(FlightStatus.Finished);
        assertNotNull(finishedFlight);
        checkFlightRoute(finishedFlight, "EGKK", "EHAM");

        checkCallsign("BAW2761");
        checkFlightStatus(FlightStatus.Preparing);
        checkFlightLastSeenIcao("EHAM");
        checkFlightRoute(null, null);
    }

    // Departure section
    public void report_1016797_1016808() {
        checkOnGround();
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightLastSeenIcao("EHAM");
        checkFlightRoute(null, null);
    }

    // Flightplan sent
    public void report_1016798() {
        checkFlightplanEvent();
        checkFlightplanData("A319", "EHAM", "EGKK");
    }

    // Taxiing out
    public void report_1016803_1016808() {
        checkFlightStatus(FlightStatus.Departing);
    }

    // Takeoff
    public void report_1016809() {
        checkTakeoffEvent();
    }

    // Flying section
    public void report_1016809_1016830() {
        checkFlying();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EHAM", null);
        checkFlightplanData("A319", "EHAM", "EGKK");
    }

    // Landing event
    public void report_1016831() {
        checkLandingEvent();
        checkFlightStatus(FlightStatus.TouchedDown);
    }

    // Arrival section
    public void report_1016831_1016833() {
        checkOnGround();
        checkFlight();
        checkFlightStatus(FlightStatus.Arrival);
        checkFlightLastSeenIcao("EHAM");
        checkFlightRoute("EHAM", "EHAM");
    }

    // No Arrived event

    // Went Offline, check Finished flight
    public void report_1016834() {
        Flight finishedFlight = getFlightFromStatusEvent(FlightStatus.Finished);
        assertNotNull(finishedFlight);
        checkFlightRoute(finishedFlight, "EHAM", "EHAM");

        checkOfflineEvent();
    }
}
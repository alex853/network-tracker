package net.simforge.networkview.flights.method.eventbased;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

@SuppressWarnings("UnusedDeclaration")
@Ignore
public class Test_Pilot1261420_670000_10000 extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-1261420_from-670000_amount-10000.csv");
        initNoOpPersistence();
        doTest(1261420, 670000, 680000);
    }

    public void report_670000_670456() {
        checkNoPilotContext();
    }



    // 670457 EPWA-.... Short connection without flight
    public void report_670457() {
        checkPositionKnown();
        checkOnlineEvent();
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
    }

    public void report_670458() {
        checkOfflineEvent();
    }

    public void report_670458_670461() {
        checkPositionUnknown();
        checkNoFlight();
    }



    // 670462 EPWA-.... Started and non-finished flight
    public void report_670462() {
        checkOnlineEvent();
    }

    public void report_670462_670473() {
        checkPositionKnown();
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightLastSeenIcao("EPWA");
        checkFlightRoute(null, null);
    }

    public void report_670471() {
        checkFlightplanEvent();
        checkFlightplanData("T154", "EPWA", "GMMX");
    }



    // 670491 EPWA-.... Non-finished flight
    public void report_670491() {
        checkOnlineEvent();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightLastSeenIcao("EPWA");
        checkFlightRoute(null, null);
        checkFlightplanEvent();
        checkFlightplanData("T154", "EPWA", "GMMX");
    }

    public void report_670493() {
        checkFlightStatus(FlightStatus.Departure);
        checkFlightRoute(null, null);
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
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EPWA", null);
    }

    // Short disconnect at 670523
    public void report_670524() {
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EPWA", null);
    }

    // Short disconnect at 670530
    public void report_670531() {
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EPWA", null);
    }

    // Short disconnect at 670537-670538
    public void report_670539() {
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EPWA", null);
    }

    public void report_670571() {
        checkFlightStatus(FlightStatus.Lost);
        checkFlightRoute("EPWA", null);
    }



    // 670573 ULLI-.... Short connection
    public void report_670573() {
        checkOnlineEvent();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightRoute(null, null);
        checkFlightplanEvent();
        checkFlightplanData("T154", "EPWA", "LEAM");
    }

    public void report_670581() {
        checkFlightplanEvent();
        checkFlightplanData("T154", "ULLI", "USRR");
    }

    public void report_670589() {
        Flight flight = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightRoute(flight, null, null);
        checkFlightplanData(flight, "T154", "ULLI", "USRR");
    }



    // 670591 ULLI-USRR
    public void report_670591() {
        checkOnlineEvent();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightRoute(null, null);
        checkFlightplanEvent();
        checkFlightplanData("T154", "ULLI", "USRR");
    }

    public void report_670595() {
        checkTakeoffEvent();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("ULLI", null);
    }

    public void report_670674() {
        checkLandingEvent();
        checkFlightStatus(FlightStatus.Arrival);
        checkFlightRoute("ULLI", "USRR");
    }

    public void report_670679() {
        Flight flight = getFlightFromStatusEvent(FlightStatus.Finished);
        checkFlightRoute(flight, "ULLI", "USRR");
        checkFlightplanData(flight, "T154", "ULLI", "USRR");
    }



    // 670702 UKBB-.... Short connection
    public void report_670705() {
        Flight flight = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightLastSeenIcao(flight,"UKBB");
        checkFlightRoute(flight, null, null);
        checkFlightplanData(flight, "T154", "ULLI", "USRR");
    }



    // 670706 UUDD-.... Short connection
    public void report_670717() {
        Flight flight = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightLastSeenIcao(flight, "UUDD");
        checkFlightRoute(flight, null, null);
        checkFlightplanData(flight, "T154", "ULLI", "USRR");
    }



    // 673328 EKYT-LDDU with disconnect
    public void report_673328() {
        checkOnlineEvent();
        checkFlightLastSeenIcao("EKYT");
        checkFlightRoute(null, null);
    }

    public void report_673338() {
        checkFlightplanEvent();
        checkFlightplanData("T154", "EKYT", "LDDU");
    }

    public void report_673364() {
        checkTakeoffEvent();
        checkFlightRoute("EKYT", null);
    }

    public void report_673405() {
        checkOfflineEvent();
        checkFlightStatus(FlightStatus.Lost);
        checkFlightRoute("EKYT", null);
    }

    public void report_673408() {
        checkOnlineEvent();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("EKYT", null);
    }

    public void report_673431() {
        checkLandingEvent();
        checkFlightStatus(FlightStatus.Arrival);
        checkFlightRoute("EKYT", "LDDU");
    }

    public void report_673436() {
        checkOfflineEvent();
        checkFlightStatusEvent(FlightStatus.Finished);
        checkNoFlight();
    }



    // 673464 URKK-UTAA with disconnect
    public void report_673464() {
        checkOnlineEvent();
        checkFlightLastSeenIcao("URKK");
        checkFlightRoute(null, null);
    }

    public void report_673490() {
        checkFlightplanEvent();
        checkFlightplanData("T154", "URKK", "UTAA");
    }

    public void report_673493() {
        checkTakeoffEvent();
    }

    public void report_673511() {
        checkOfflineEvent();
        checkFlightStatus(FlightStatus.Lost);
        checkFlightRoute("URKK", null);
    }

    public void report_673512() {
        checkOnlineEvent();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("URKK", null);
    }

    public void report_673562() {
        checkLandingEvent();
        checkFlightStatus(FlightStatus.Arrival);
        checkFlightRoute("URKK", "UTAA");
    }

    public void report_673563() {
        checkOfflineEvent();
        checkFlightStatusEvent(FlightStatus.Finished);
        checkNoFlight();
    }



    // 673566 UKFF-.... Short connection
    public void report_673568() {
        Flight terminated = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightLastSeenIcao(terminated,"UKFF");
        checkFlightRoute(terminated, null, null);
        checkFlightplanData(terminated, "T154", "URKK", "UTAA");
    }



    // 674024 CYVR-.... with very long disconnect (about 2 hours), flight without landing
    public void report_674024() {
        checkOnlineEvent();
        checkFlight();
        checkFlightLastSeenIcao("CYVR");
        checkFlightRoute(null, null);
    }

    public void report_674064() {
        checkFlightLastSeenIcao("CYVR");
        checkFlightRoute(null, null);
        checkFlightplanEvent();
        checkFlightplanData("T154", "CYVR", "MMMX");
    }

    public void report_674116() {
        checkTakeoffEvent();
        checkFlightStatusEvent(FlightStatus.Flying);
        checkFlightRoute("CYVR", null);
    }

    public void report_674124() {
        checkOfflineEvent();
        checkFlightStatusEvent(FlightStatus.Lost);
        checkFlightRoute("CYVR", null);
    }

    /**
     * The previous flight terminates, Ellipse is OK, Track trail fails
     * Positions do not seem well, I would accept the discontinuity in this case
     * @see net.simforge.networkview.flights.method.eventbased.criteria.EllipseCriterion
     * @see net.simforge.networkview.flights.method.eventbased.criteria.TrackTrailCriterion
     */
    public void report_674195() {
        checkOnlineEvent();
        checkFlightStatusEvent(FlightStatus.Flying);
        checkFlightRoute(null, null);
    }

    public void report_674198() {
        checkOfflineEvent();
        checkFlightStatusEvent(FlightStatus.Lost);
        checkFlightRoute(null, null);
    }

    public void report_674314() {
        checkPositionUnknown();
        checkFlightStatus(FlightStatus.Lost);
        checkFlightRoute(null, null);
        checkFlightplanData("T154", "CYVR", "MMMX");
    }



    // 674315 UKHH-LGSA
    public void report_674315() {
        checkOnlineEvent();
        checkFlight();
        checkFlightStatusEvent(FlightStatus.Preparing);
    }

    public void report_674315_674325() {
        checkPositionKnown();
        checkOnGround();
        checkFlightStatus(FlightStatus.Departure);
        checkFlightLastSeenIcao("UKHH");
        checkFlightRoute(null, null);
    }

    public void report_674320() {
        checkFlightplanEvent();
        checkFlightplanData("T154", "UKHH", "LGSA");
    }

    public void report_674324() {
        checkFlightStatus(FlightStatus.Departing);
    }

    public void report_674326() {
        checkTakeoffEvent();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightStatusEvent(FlightStatus.Flying);
        checkFlightplanData("T154", "UKHH", "LGSA");
    }

    public void report_674326_674394() {
        checkFlying();
        checkFlightplanData("T154", "UKHH", "LGSA");
        checkFlightStatus(FlightStatus.Flying);
    }

    public void report_674395() {
        checkLandingEvent();
        checkFlightStatus(FlightStatus.Arrival);
        checkFlightRoute("UKHH", "LGSA");
        checkFlightStatusEvent(FlightStatus.Arriving);
    }

    public void report_674398() {
        checkOfflineEvent();
        checkFlightStatusEvent(FlightStatus.Finished);
        checkNoFlight();
    }

    public void report_674400_674804() {
        checkNoPilotContextOrPositionUnknown();
        checkNoFlight();
        checkNoEvents();
    }



    // 674805 UHMA-.... Short connection
    public void report_674811() {
        Flight terminated = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightLastSeenIcao(terminated, "UHMA");
        checkFlightRoute(terminated, null, null);
    }



    // 674817 UHMA-.... Non-finished flight
    public void report_674817() {
        checkOnlineEvent();
        checkFlightLastSeenIcao("UHMA");
        checkFlightRoute(null, null);
    }

    public void report_674829() {
        checkFlightplanEvent();
        checkFlightplanData("T154", "UHMA", "PAOT");
    }

    public void report_674837() {
        checkTakeoffEvent();
    }

    public void report_674843() {
        Flight flight = getFlightFromStatusEvent(FlightStatus.Lost);
        checkFlightRoute(flight, "UHMA", null);
    }



    // 674846 UUDD-UTTT Takeoff, several disconnections and reconnections, landing
    public void report_674846() {
        checkOnlineEvent();
        checkFlightLastSeenIcao("UUDD");
        checkFlightRoute(null, null);
    }

    public void report_674860() {
        checkFlightplanEvent();
        checkFlightplanData("T154", "UUDD", "UTTT");
    }

    public void report_674862() {
        checkTakeoffEvent();
    }

    public void report_674863() {
        checkOfflineEvent();
        checkFlightStatus(FlightStatus.Lost);
        checkFlightRoute("UUDD", null);
    }

    public void report_674898() {
        checkOnlineEvent();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("UUDD", null);
        checkFlightplanData("T154", "UUDD", "UTTT");
    }

    public void report_674907() {
        checkOfflineEvent();
        checkFlightStatus(FlightStatus.Lost);
        checkFlightRoute("UUDD", null);
    }

    public void report_674911() {
        checkOnlineEvent();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("UUDD", null);
        checkFlightplanData("T154", "UUDD", "UTTT");
    }

    public void report_674953() {
        checkOfflineEvent();
        checkFlightStatus(FlightStatus.Lost);
        checkFlightRoute("UUDD", null);
    }

    public void report_674978() {
        checkOnlineEvent();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("UUDD", null);
        checkFlightplanData("T154", "UUDD", "UTTT");
    }

    public void report_674981() {
        checkOfflineEvent();
        checkFlightStatus(FlightStatus.Lost);
        checkFlightRoute("UUDD", null);
    }

    public void report_674982() {
        checkOnlineEvent();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("UUDD", null);
        checkFlightplanData("T154", "UUDD", "UTTT");
    }

    public void report_674997() {
        checkLandingEvent();
        checkFlightStatus(FlightStatus.Arrival);
        checkFlightRoute("UUDD", "UTTT");
    }

    public void report_675002() {
        checkOfflineEvent();
        checkFlightStatusEvent(FlightStatus.Finished);
        checkNoFlight();
    }



    // 675003 UUDD-.... Short connection
    public void report_675003_675008() {
        checkFlightRoute("UUDD", null);
    }

    // 675009 Jump to UWWW, UWWW-.... Short connection
    public void report_675009() {
        Flight terminatedflight = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightRoute(terminatedflight, "UUDD", null);

        Flight departureflight = getFlightFromStatusEvent(FlightStatus.Departure);
        checkFlightRoute(departureflight, "UWWW", null);
    }

    public void report_675035() {
        Flight flight = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightRoute(flight, "UWWW", null);
    }

    public void report_675036_675477() {
        checkPositionUnknown();
        checkNoFlight();
        checkNoEvents();
    }



    // 675478 UWWW-.... Lost flight
    public void report_675478() {
        checkOnlineEvent();
        checkFlightRoute("UWWW", null);
    }

    public void report_675492() {
        checkFlightplanEvent();
        checkFlightplanData("T154", "UWWW", "EDDF");
    }

    public void report_675495() {
        checkTakeoffEvent();
    }

    public void report_675517() {
        Flight lost = getFlightFromStatusEvent(FlightStatus.Lost);
        checkFlightRoute(lost, "UWWW", null);
    }



    // 675527 UWWW-.... Short connection
    public void report_675534() {
        Flight terminated = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightRoute(terminated, "UWWW", null);
    }



    // 675553 UUDD-.... Short connection
    public void report_675560() {
        Flight terminated = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightRoute(terminated, "UUDD", null);
    }



    // 675563 UUDD-.... Short connection
    public void report_675578() {
        Flight terminated = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightRoute(terminated, "UUDD", null);
        checkFlightplanData(terminated, "T154", "UWWW", "EDDF");
    }



    // 675595 ....-EDDF Finished flight without takeoff
    // postponed 675595 can be rejoined with 675578, however I'm not sure that we need to try to join it
    public void report_675595() {
        checkOnlineEvent();
        checkFlight();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute(null, null);
        checkFlightplanData("T154", "UUDD", "EDDF");
    }

    public void report_675654() {
        checkLandingEvent();
        checkFlightStatus(FlightStatus.Arrival);
        checkFlightRoute(null, "EDDF");
        checkFlightplanData("T154", "UUDD", "EDDF");
    }

    public void report_675660() {
        Flight finished = getFlightFromStatusEvent(FlightStatus.Finished);
        checkFlightRoute(finished, null, "EDDF");
        checkFlightplanData(finished, "T154", "UUDD", "EDDF");
    }



    // 675659 EDDF-.... Short connection
    public void report_675666() {
        Flight terminated = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightRoute(terminated, "EDDF", null);
    }



    // 675669 UWWW-UWWW Local flight with disconnect
    public void report_675669_675680() {
        checkFlightStatus(FlightStatus.Departure);
        checkFlightRoute("UWWW", null);
    }

    public void report_675681() {
        checkTakeoffEvent();
        checkFlightRoute("UWWW", null);
    }

    public void report_675683() {
        checkOfflineEvent();
        checkFlightStatus(FlightStatus.Lost);
        checkFlightRoute("UWWW", null);
    }

    public void report_675698() {
        checkOnlineEvent();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("UWWW", null);
    }

    public void report_675707() {
        checkLandingEvent();
        checkFlightStatus(FlightStatus.Arrival);
        checkFlightRoute("UWWW", "UWWW");
    }



    // 675712 Another one UWWW-UWWW Local flight
    public void report_675713() {
        Flight finished = getFlightFromStatusEvent(FlightStatus.Finished);
        checkFlightRoute(finished, "UWWW", "UWWW");

        Flight departing = getFlightFromStatusEvent(FlightStatus.Departure);
        checkFlightRoute(departing, "UWWW", null);
    }

    public void report_675723() {
        checkTakeoffEvent();
        checkFlightRoute("UWWW", null);
    }

    public void report_675735() {
        checkLandingEvent();
        checkFlightStatus(FlightStatus.Arrival);
        checkFlightRoute("UWWW", "UWWW");
    }



    // 675736 Touch&Go! And another UWWW-UWWW local flight
    public void report_675736() {
        Flight finished = getFlightFromStatusEvent(FlightStatus.Finished);
        checkFlightRoute(finished, "UWWW", "UWWW");

        Flight departed = getFlightFromStatusEvent(FlightStatus.Departure);
        checkFlightRoute(departed, "UWWW", null);

        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("UWWW", null);
    }

    public void report_675760() {
        checkLandingEvent();
    }

    public void report_675763() {
        Flight finished = getFlightFromStatusEvent(FlightStatus.Finished);
        checkFlightRoute(finished, "UWWW", "UWWW");
    }



    // 677682 UWWW-LUKK filled ad UWWW-LIRN
    public void report_677767() {
        checkLandingEvent();
        checkFlightRoute("UWWW", "LUKK");
        checkFlightplanData("T154", "UWWW", "LIRN");
    }



    // 677772 LUKK-LIRN second part of UWWW-LIRN sequence
    public void report_677773() {
        Flight finished = getFlightFromStatusEvent(FlightStatus.Finished);
        checkFlightRoute(finished, "UWWW", "LUKK");

        Flight departed = getFlightFromStatusEvent(FlightStatus.Departure);
        checkFlightRoute(departed, "LUKK", null);
        checkFlightplanData(departed, "T154", "UWWW", "LIRN");
    }

    public void report_677837() {
        Flight finished = getFlightFromStatusEvent(FlightStatus.Finished);
        checkFlightRoute(finished, "LUKK", "LIRN");
        checkFlightplanData(finished, "T154", "UWWW", "LIRN");
    }



    // 677842 UUWW-.... Short connection
    public void report_677893() {
        Flight terminated = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightRoute(terminated, "UUWW", null);
    }



    // 678347 LFRS-UWWW
    public void report_678572() {
        checkFlightStatus(FlightStatus.Arrival);
        checkFlightRoute("LFRS", "UWWW");
        checkFlightplanData("T154", "LFRS", "UWWW");
    }



    // 678573 UWWW-.... Short connection
    public void report_678573() {
        Flight finished = getFlightFromStatusEvent(FlightStatus.Finished);
        checkFlightRoute(finished, "LFRS", "UWWW");
        checkFlightplanData(finished, "T154", "LFRS", "UWWW");

        checkFlightRoute("UWWW", null);
    }

    public void report_678582() {
        Flight terminated = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightRoute(terminated, "UWWW", null);
    }



    // 678585 LTCG-URSS Non-finished flight which was terminated due to time endurance
    public void report_678621() {
        checkOfflineEvent();
        checkFlight();
        checkFlightStatus(FlightStatus.Lost);
        checkFlightRoute("LTCG", null);
        checkFlightplanData("T154", "LTCG", "URSS");
    }

    // this conditions may fail after changing of aircraft endurance code or report time difference calculation
    public void report_678806() {
        checkFlightStatusEvent(FlightStatus.Terminated);
        Flight terminated = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightRoute(terminated, "LTCG", null);
    }



    // 679081 LCLK-UWWW
    public void report_679224() {
        checkLandingEvent();
        checkFlightRoute("LCLK", "UWWW");
        checkFlightplanData("T154", "LCLK", "UWWW");
    }

    // 679229 UWWW-.... Short connection
    public void report_679230() {
        Flight finished = getFlightFromStatusEvent(FlightStatus.Finished);
        checkFlightRoute(finished, "LCLK", "UWWW");
        checkFlightplanData(finished, "T154", "LCLK", "UWWW");

        checkFlightRoute("UWWW", null);
    }

    public void report_679232() {
        Flight terminated = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightRoute(terminated, "UWWW", null);
    }



    // 679261 OPRN-.... Non-finished flight
    public void report_679316() {
        Flight lost = getFlightFromStatusEvent(FlightStatus.Lost);
        checkFlightRoute(lost, "OPRN", null);
        checkFlightplanData(lost, "T154", "OPRN", "UWWW");
    }



    // 679320 ULLI-.... Short connection
    public void report_679348() {
        Flight terminated = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightRoute(terminated, "ULLI", null);
    }



    // 679817 ULMM-UWWW
    public void report_679903() {
        checkLandingEvent();
        checkFlightRoute("ULMM", "UWWW");
        checkFlightplanData("T154", "ULMM", "UWWW");
    }

    public void report_679909() {
        Flight finished = getFlightFromStatusEvent(FlightStatus.Finished);
        checkFlightRoute(finished, "ULMM", "UWWW");
        checkFlightplanData(finished, "T154", "ULMM", "UWWW");
    }



    // 679912 DTTA-.... Non-finished flight (end of file)
    public void report_679999() {
        checkFlight();
        checkFlightStatus(FlightStatus.Flying);
        checkFlightRoute("DTTA", null);
        checkFlightplanData("T154", "DTTA", "UWWW");
    }
}

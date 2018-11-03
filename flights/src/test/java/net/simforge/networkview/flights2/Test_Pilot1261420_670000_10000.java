package net.simforge.networkview.flights2;

import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.flight.FlightStatus;
import org.junit.Test;

import java.io.IOException;

@SuppressWarnings("UnusedDeclaration")
public class Test_Pilot1261420_670000_10000 extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-1261420_from-670000_amount-10000.csv");
        initNoOpPersistence();
        doTest(1261420, 670000, 680000);
    }

    public void report_670000_670456() {
        checkPositionUnknown();
    }



    // 670457 EPWA-.... Short connection without flight
    public void report_670457() {
        checkPositionKnown();
        checkOnlineEvent();
        checkMovement();
        checkMovementStatus(FlightStatus.Departure);
    }

    public void report_670458() {
        checkOfflineEvent();
    }

    public void report_670458_670461() {
        checkPositionUnknown();
        checkNoMovement();
    }



    // 670462 EPWA-.... Started and non-finished flight
    public void report_670462() {
        checkOnlineEvent();
    }

    public void report_670462_670473() {
        checkPositionKnown();
        checkMovement();
        checkMovementStatus(FlightStatus.Departure);
        checkMovementRoute("EPWA", null);
    }

    public void report_670471() {
        checkMovementFlightplanEvent();
        checkMovementFlightplanData("B/T154/G", "EPWA", "GMMX");
    }



    // 670491 EPWA-.... Non-finished flight
    public void report_670491() {
        checkOnlineEvent();
        checkMovementStatus(FlightStatus.Departure);
        checkMovementRoute("EPWA", null);
        checkMovementFlightplanEvent();
        checkMovementFlightplanData("B/T154/G", "EPWA", "GMMX");
    }

    public void report_670493() {
        checkMovementStatus(FlightStatus.Departure);
        checkMovementRoute("EPWA", null);
        checkMovementFlightplanEvent();
        checkMovementFlightplanData("B/T154/G", "EPWA", "LEAM");
    }

    public void report_670497() {
        checkTakeoffEvent();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("EPWA", null);
    }

    public void report_670519() {
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("EPWA", null);
    }

    public void report_670524() {
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("EPWA", null);
    }

    public void report_670531() {
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("EPWA", null);
    }

    public void report_670539() {
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("EPWA", null);
    }

    public void report_670571() {
        checkMovementStatus(FlightStatus.Lost);
        checkMovementRoute("EPWA", null);
    }



    // 670573 ULLI-.... Short connection
    public void report_670573() {
        checkOnlineEvent();
        checkMovementStatus(FlightStatus.Departure);
        checkMovementRoute("ULLI", null);
        checkMovementFlightplanEvent();
        checkMovementFlightplanData("B/T154/G", "EPWA", "LEAM");
    }

    public void report_670581() {
        checkMovementFlightplanEvent();
        checkMovementFlightplanData("B/T154/G", "ULLI", "USRR");
    }

    public void report_670589() {
        Flight movement = getMovementFromStatusEvent(FlightStatus.Terminated);
        checkMovementRoute(movement, "ULLI", null);
        checkMovementFlightplanData(movement, "B/T154/G", "ULLI", "USRR");
    }



    // 670591 ULLI-USRR
    public void report_670591() {
        checkOnlineEvent();
        checkMovementStatus(FlightStatus.Departure);
        checkMovementRoute("ULLI", null);
        checkMovementFlightplanEvent();
        checkMovementFlightplanData("B/T154/G", "ULLI", "USRR");
    }

    public void report_670595() {
        checkTakeoffEvent();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("ULLI", null);
    }

    public void report_670674() {
        checkLandingEvent();
        checkMovementStatus(FlightStatus.Arrival);
        checkMovementRoute("ULLI", "USRR");
    }

    public void report_670679() {
        Flight movement = getMovementFromStatusEvent(FlightStatus.Finished);
        checkMovementRoute(movement, "ULLI", "USRR");
        checkMovementFlightplanData(movement, "B/T154/G", "ULLI", "USRR");
    }



    // 670702 UKBB-.... Short connection
    public void report_670705() {
        Flight movement = getMovementFromStatusEvent(FlightStatus.Terminated);
        checkMovementRoute(movement, "UKBB", null);
        checkMovementFlightplanData(movement, "B/T154/G", "ULLI", "USRR");
    }



    // 670706 UUDD-.... Short connection
    public void report_670717() {
        Flight movement = getMovementFromStatusEvent(FlightStatus.Terminated);
        checkMovementRoute(movement, "UUDD", null);
        checkMovementFlightplanData(movement, "B/T154/G", "ULLI", "USRR");
    }



    // 673328 EKYT-LDDU with disconnect
    public void report_673328() {
        checkOnlineEvent();
        checkMovementRoute("EKYT", null);
    }

    public void report_673338() {
        checkMovementFlightplanEvent();
        checkMovementFlightplanData("B/T154/G", "EKYT", "LDDU");
    }

    public void report_673364() {
        checkTakeoffEvent();
    }

    public void report_673405() {
        checkOfflineEvent();
        checkMovementStatus(FlightStatus.Lost);
        checkMovementRoute("EKYT", null);
    }

    public void report_673408() {
        checkOnlineEvent();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("EKYT", null);
    }

    public void report_673431() {
        checkLandingEvent();
        checkMovementStatus(FlightStatus.Arrival);
        checkMovementRoute("EKYT", "LDDU");
    }

    public void report_673436() {
        checkOfflineEvent();
        checkMovementStatusEvent(FlightStatus.Finished);
        checkNoMovement();
    }



    // 673464 URKK-UTAA with disconnect
    public void report_673464() {
        checkOnlineEvent();
        checkMovementRoute("URKK", null);
    }

    public void report_673490() {
        checkMovementFlightplanEvent();
        checkMovementFlightplanData("B/T154/G", "URKK", "UTAA");
    }

    public void report_673493() {
        checkTakeoffEvent();
    }

    public void report_673511() {
        checkOfflineEvent();
        checkMovementStatus(FlightStatus.Lost);
        checkMovementRoute("URKK", null);
    }

    public void report_673512() {
        checkOnlineEvent();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("URKK", null);
    }

    public void report_673562() {
        checkLandingEvent();
        checkMovementStatus(FlightStatus.Arrival);
        checkMovementRoute("URKK", "UTAA");
    }

    public void report_673563() {
        checkOfflineEvent();
        checkMovementStatusEvent(FlightStatus.Finished);
        checkNoMovement();
    }



    // 673566 UKFF-.... Short connection
    public void report_673568() {
        Flight terminated = getMovementFromStatusEvent(FlightStatus.Terminated);
        checkMovementRoute(terminated, "UKFF", null);
        checkMovementFlightplanData(terminated, "B/T154/G", "URKK", "UTAA");
    }



    // 674024 CYVR-.... with very long disconnect (about 2 hours), flight without landing
    public void report_674024() {
        checkOnlineEvent();
        checkMovement();
        checkMovementRoute("CYVR", null);
    }

    public void report_674064() {
        checkMovementRoute("CYVR", null);
        checkMovementFlightplanEvent();
        checkMovementFlightplanData("B/T154/G", "CYVR", "MMMX");
    }

    public void report_674116() {
        checkTakeoffEvent();
        checkMovementStatusEvent(FlightStatus.Flying);
        checkMovementRoute("CYVR", null);
    }

    public void report_674124() {
        checkOfflineEvent();
        checkMovementStatusEvent(FlightStatus.Lost);
        checkMovementRoute("CYVR", null);
    }

    public void report_674195() {
        checkOnlineEvent();
        checkMovementStatusEvent(FlightStatus.Flying);
        checkMovementRoute("CYVR", null);
    }

    public void report_674198() {
        checkOfflineEvent();
        checkMovementStatusEvent(FlightStatus.Lost);
        checkMovementRoute("CYVR", null);
    }

    public void report_674314() {
        checkPositionUnknown();
        checkMovementStatus(FlightStatus.Lost);
        checkMovementRoute("CYVR", null);
        checkMovementFlightplanData("B/T154/G", "CYVR", "MMMX");
    }



    // 674315 UKHH-LGSA
    public void report_674315() {
        checkOnlineEvent();
        checkMovement();
        checkMovementStatusEvent(FlightStatus.Departure); // postponed departure.preparing
    }

    public void report_674315_674325() {
        checkPositionKnown();
        checkOnGround();
        checkMovementStatus(FlightStatus.Departure);
        checkMovementRoute("UKHH", null);
    }

    public void report_674320() {
        checkMovementFlightplanEvent();
        checkMovementFlightplanData("B/T154/G", "UKHH", "LGSA");
    }

    public void report_674324() {
        //checkMovementStatusEvent("departure.departing"); // postponed departure.departing
    }

    public void report_674326() {
        checkTakeoffEvent();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementStatusEvent(FlightStatus.Flying);
        checkMovementFlightplanData("B/T154/G", "UKHH", "LGSA");
    }

    public void report_674326_674394() {
        checkFlying();
        checkMovementFlightplanData("B/T154/G", "UKHH", "LGSA");
        checkMovementStatus(FlightStatus.Flying);
    }

    public void report_674395() {
        checkLandingEvent();
        checkMovementStatus(FlightStatus.Arrival);
        checkMovementRoute("UKHH", "LGSA");
        checkMovementStatusEvent(FlightStatus.Arrival); // postponed arrival.landing
    }

    public void report_674398() {
        checkOfflineEvent();
        checkMovementStatusEvent(FlightStatus.Finished);
        checkNoMovement();
    }

    public void report_674400_674804() {
        checkPositionUnknown();
        checkNoMovement();
        checkNoEvents();
    }



    // 674805 UHMA-.... Short connection
    public void report_674811() {
        Flight terminated = getMovementFromStatusEvent(FlightStatus.Terminated);
        checkMovementRoute(terminated, "UHMA", null);
    }



    // 674817 UHMA-.... Non-finished flight
    public void report_674817() {
        checkOnlineEvent();
        checkMovementRoute("UHMA", null);
    }

    public void report_674829() {
        checkMovementFlightplanEvent();
        checkMovementFlightplanData("B/T154/G", "UHMA", "PAOT");
    }

    public void report_674837() {
        checkTakeoffEvent();
    }

    public void report_674843() {
        Flight movement = getMovementFromStatusEvent(FlightStatus.Lost);
        checkMovementRoute(movement, "UHMA", null);
    }



    // 674846 UUDD-UTTT Takeoff, several disconnections and reconnections, landing
    public void report_674846() {
        checkOnlineEvent();
        checkMovementRoute("UUDD", null);
    }

    public void report_674860() {
        checkMovementFlightplanEvent();
        checkMovementFlightplanData("B/T154/G", "UUDD", "UTTT");
    }

    public void report_674862() {
        checkTakeoffEvent();
    }

    public void report_674863() {
        checkOfflineEvent();
        checkMovementStatus(FlightStatus.Lost);
        checkMovementRoute("UUDD", null);
    }

    public void report_674898() {
        checkOnlineEvent();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("UUDD", null);
        checkMovementFlightplanData("B/T154/G", "UUDD", "UTTT");
    }

    public void report_674907() {
        checkOfflineEvent();
        checkMovementStatus(FlightStatus.Lost);
        checkMovementRoute("UUDD", null);
    }

    public void report_674911() {
        checkOnlineEvent();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("UUDD", null);
        checkMovementFlightplanData("B/T154/G", "UUDD", "UTTT");
    }

    public void report_674953() {
        checkOfflineEvent();
        checkMovementStatus(FlightStatus.Lost);
        checkMovementRoute("UUDD", null);
    }

    public void report_674978() {
        checkOnlineEvent();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("UUDD", null);
        checkMovementFlightplanData("B/T154/G", "UUDD", "UTTT");
    }

    public void report_674981() {
        checkOfflineEvent();
        checkMovementStatus(FlightStatus.Lost);
        checkMovementRoute("UUDD", null);
    }

    public void report_674982() {
        checkOnlineEvent();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("UUDD", null);
        checkMovementFlightplanData("B/T154/G", "UUDD", "UTTT");
    }

    public void report_674997() {
        checkLandingEvent();
        checkMovementStatus(FlightStatus.Arrival);
        checkMovementRoute("UUDD", "UTTT");
    }

    public void report_675002() {
        checkOfflineEvent();
        checkMovementStatusEvent(FlightStatus.Finished);
        checkNoMovement();
    }



    // 675003 UUDD-.... Short connection
    public void report_675003_675008() {
        checkMovementRoute("UUDD", null);
    }

    // 675009 Jump to UWWW, UWWW-.... Short connection
    public void report_675009() {
        Flight terminatedMovement = getMovementFromStatusEvent(FlightStatus.Terminated);
        checkMovementRoute(terminatedMovement, "UUDD", null);

        Flight departureMovement = getMovementFromStatusEvent(FlightStatus.Departure);
        checkMovementRoute(departureMovement, "UWWW", null);
    }

    public void report_675035() {
        Flight movement = getMovementFromStatusEvent(FlightStatus.Terminated);
        checkMovementRoute(movement, "UWWW", null);
    }

    public void report_675036_675477() {
        checkPositionUnknown();
        checkNoMovement();
        checkNoEvents();
    }



    // 675478 UWWW-.... Lost flight
    public void report_675478() {
        checkOnlineEvent();
        checkMovementRoute("UWWW", null);
    }

    public void report_675492() {
        checkMovementFlightplanEvent();
        checkMovementFlightplanData("B/T154/G", "UWWW", "EDDF");
    }

    public void report_675495() {
        checkTakeoffEvent();
    }

    public void report_675517() {
        Flight lost = getMovementFromStatusEvent(FlightStatus.Lost);
        checkMovementRoute(lost, "UWWW", null);
    }



    // 675527 UWWW-.... Short connection
    public void report_675534() {
        Flight terminated = getMovementFromStatusEvent(FlightStatus.Terminated);
        checkMovementRoute(terminated, "UWWW", null);
    }



    // 675553 UUDD-.... Short connection
    public void report_675560() {
        Flight terminated = getMovementFromStatusEvent(FlightStatus.Terminated);
        checkMovementRoute(terminated, "UUDD", null);
    }



    // 675563 UUDD-.... Short connection
    public void report_675578() {
        Flight terminated = getMovementFromStatusEvent(FlightStatus.Terminated);
        checkMovementRoute(terminated, "UUDD", null);
        checkMovementFlightplanData(terminated, "B/T154/G", "UWWW", "EDDF");
    }



    // 675595 ....-EDDF Finished flight without takeoff
    // postponed 675595 can be rejoined with 675578, however I'm not sure that we need to try to join it
    public void report_675595() {
        checkOnlineEvent();
        checkMovement();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute(null, null);
        checkMovementFlightplanData("B/T154/G", "UUDD", "EDDF");
    }

    public void report_675654() {
        checkLandingEvent();
        checkMovementStatus(FlightStatus.Arrival);
        checkMovementRoute(null, "EDDF");
        checkMovementFlightplanData("B/T154/G", "UUDD", "EDDF");
    }

    public void report_675660() {
        Flight finished = getMovementFromStatusEvent(FlightStatus.Finished);
        checkMovementRoute(finished, null, "EDDF");
        checkMovementFlightplanData(finished, "B/T154/G", "UUDD", "EDDF");
    }



    // 675659 EDDF-.... Short connection
    public void report_675666() {
        Flight terminated = getMovementFromStatusEvent(FlightStatus.Terminated);
        checkMovementRoute(terminated, "EDDF", null);
    }



    // 675669 UWWW-UWWW Local flight with disconnect
    public void report_675669_675680() {
        checkMovementStatus(FlightStatus.Departure);
        checkMovementRoute("UWWW", null);
    }

    public void report_675681() {
        checkTakeoffEvent();
        checkMovementRoute("UWWW", null);
    }

    public void report_675683() {
        checkOfflineEvent();
        checkMovementStatus(FlightStatus.Lost);
        checkMovementRoute("UWWW", null);
    }

    public void report_675698() {
        checkOnlineEvent();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("UWWW", null);
    }

    public void report_675707() {
        checkLandingEvent();
        checkMovementStatus(FlightStatus.Arrival);
        checkMovementRoute("UWWW", "UWWW");
    }



    // 675712 Another one UWWW-UWWW Local flight
    public void report_675713() {
        Flight finished = getMovementFromStatusEvent(FlightStatus.Finished);
        checkMovementRoute(finished, "UWWW", "UWWW");

        Flight departing = getMovementFromStatusEvent(FlightStatus.Departure);
        checkMovementRoute(departing, "UWWW", null);
    }

    public void report_675723() {
        checkTakeoffEvent();
        checkMovementRoute("UWWW", null);
    }

    public void report_675735() {
        checkLandingEvent();
        checkMovementStatus(FlightStatus.Arrival);
        checkMovementRoute("UWWW", "UWWW");
    }



    // 675736 Touch&Go! And another UWWW-UWWW local flight
    public void report_675736() {
        Flight finished = getMovementFromStatusEvent(FlightStatus.Finished);
        checkMovementRoute(finished, "UWWW", "UWWW");

        Flight departed = getMovementFromStatusEvent(FlightStatus.Departure);
        checkMovementRoute(departed, "UWWW", null);

        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("UWWW", null);
    }

    public void report_675760() {
        checkLandingEvent();
    }

    public void report_675763() {
        Flight finished = getMovementFromStatusEvent(FlightStatus.Finished);
        checkMovementRoute(finished, "UWWW", "UWWW");
    }



    // 677682 UWWW-LUKK filled ad UWWW-LIRN
    public void report_677767() {
        checkLandingEvent();
        checkMovementRoute("UWWW", "LUKK");
        checkMovementFlightplanData("B/T154/G", "UWWW", "LIRN");
    }



    // 677772 LUKK-LIRN second part of UWWW-LIRN sequence
    public void report_677773() {
        Flight finished = getMovementFromStatusEvent(FlightStatus.Finished);
        checkMovementRoute(finished, "UWWW", "LUKK");

        Flight departed = getMovementFromStatusEvent(FlightStatus.Departure);
        checkMovementRoute(departed, "LUKK", null);
        checkMovementFlightplanData(departed, "B/T154/G", "UWWW", "LIRN");
    }

    public void report_677837() {
        Flight finished = getMovementFromStatusEvent(FlightStatus.Finished);
        checkMovementRoute(finished, "LUKK", "LIRN");
        checkMovementFlightplanData(finished, "B/T154/G", "UWWW", "LIRN");
    }



    // 677842 UUWW-.... Short connection
    public void report_677893() {
        Flight terminated = getMovementFromStatusEvent(FlightStatus.Terminated);
        checkMovementRoute(terminated, "UUWW", null);
    }



    // 678347 LFRS-UWWW
    public void report_678572() {
        checkMovementStatus(FlightStatus.Arrival);
        checkMovementRoute("LFRS", "UWWW");
        checkMovementFlightplanData("B/T154/G", "LFRS", "UWWW");
    }



    // 678573 UWWW-.... Short connection
    public void report_678573() {
        Flight finished = getMovementFromStatusEvent(FlightStatus.Finished);
        checkMovementRoute(finished, "LFRS", "UWWW");
        checkMovementFlightplanData(finished, "B/T154/G", "LFRS", "UWWW");

        checkMovementRoute("UWWW", null);
    }

    public void report_678582() {
        Flight terminated = getMovementFromStatusEvent(FlightStatus.Terminated);
        checkMovementRoute(terminated, "UWWW", null);
    }



    // 678585 LTCG-URSS Non-finished flight which was terminated due to time endurance
    public void report_678621() {
        checkOfflineEvent();
        checkMovement();
        checkMovementStatus(FlightStatus.Lost);
        checkMovementRoute("LTCG", null);
        checkMovementFlightplanData("B/T154/G", "LTCG", "URSS");
    }

    // this conditions may fail after changing of aircraft endurance code or report time difference calculation
    public void report_678806() {
        checkMovementStatusEvent(FlightStatus.Terminated);
        Flight terminated = getMovementFromStatusEvent(FlightStatus.Terminated);
        checkMovementRoute(terminated, "LTCG", null);
    }



    // 679081 LCLK-UWWW
    public void report_679224() {
        checkLandingEvent();
        checkMovementRoute("LCLK", "UWWW");
        checkMovementFlightplanData("B/T154/G", "LCLK", "UWWW");
    }

    // 679229 UWWW-.... Short connection
    public void report_679230() {
        Flight finished = getMovementFromStatusEvent(FlightStatus.Finished);
        checkMovementRoute(finished, "LCLK", "UWWW");
        checkMovementFlightplanData(finished, "B/T154/G", "LCLK", "UWWW");

        checkMovementRoute("UWWW", null);
    }

    public void report_679232() {
        Flight terminated = getMovementFromStatusEvent(FlightStatus.Terminated);
        checkMovementRoute(terminated, "UWWW", null);
    }



    // 679261 OPRN-.... Non-finished flight
    public void report_679316() {
        Flight lost = getMovementFromStatusEvent(FlightStatus.Lost);
        checkMovementRoute(lost, "OPRN", null);
        checkMovementFlightplanData(lost, "B/T154/G", "OPRN", "UWWW");
    }



    // 679320 ULLI-.... Short connection
    public void report_679348() {
        Flight terminated = getMovementFromStatusEvent(FlightStatus.Terminated);
        checkMovementRoute(terminated, "ULLI", null);
    }



    // 679817 ULMM-UWWW
    public void report_679903() {
        checkLandingEvent();
        checkMovementRoute("ULMM", "UWWW");
        checkMovementFlightplanData("B/T154/G", "ULMM", "UWWW");
    }

    public void report_679909() {
        Flight finished = getMovementFromStatusEvent(FlightStatus.Finished);
        checkMovementRoute(finished, "ULMM", "UWWW");
        checkMovementFlightplanData(finished, "B/T154/G", "ULMM", "UWWW");
    }



    // 679912 DTTA-.... Non-finished flight (end of file)
    public void report_679999() {
        checkMovement();
        checkMovementStatus(FlightStatus.Flying);
        checkMovementRoute("DTTA", null);
        checkMovementFlightplanData("B/T154/G", "DTTA", "UWWW");
    }
}

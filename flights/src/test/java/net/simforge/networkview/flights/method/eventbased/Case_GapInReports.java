package net.simforge.networkview.flights.method.eventbased;

import net.simforge.networkview.flights.EllipseCriterion;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * Pilot was on ground when Datafeeder went down for few days.
 * When Datafeeder restored the pilot was seen cruising the other flight.
 *
 * Expected behaviour:
 *   1) The first flight terminates.
 *   2) The second flight starts as "in air" flight.
 */
@Ignore
public class Case_GapInReports extends BaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-1096198_from-1000000_amount-3227.csv");
        initNoOpPersistence();
        doTest(1096198, 1000620, 1000645);
    }

    public void report_1000620_1000640() {
        checkFlight();
        checkFlightStatus(FlightStatus.Departure);
        checkCallsign("SHT26");
        checkFlightRoute(null, null);
        checkFlightplanData("B773", "EGKK", "EGPH");
    }

    /**
     * @see net.simforge.networkview.flights.method.eventbased.criteria.TrackTrailCriterion
     * @see EllipseCriterion
     */
    public void report_1000641() {
        checkFlightStatusEvent(FlightStatus.Terminated);
        Flight terminatedFlight = getFlightFromStatusEvent(FlightStatus.Terminated);
        checkFlightplanData(terminatedFlight, "B773", "EGKK", "EGPH");
    }

    public void report_1000641_1000645() {
        checkFlight();
        checkFlightStatus(FlightStatus.Flying);
        checkCallsign("EZY29");
        checkFlightRoute(null, null);
        checkFlightplanData("B738", "EGPH", "LFPG");
    }
}

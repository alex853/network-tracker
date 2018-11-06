package net.simforge.networkview.flights3;

import net.simforge.networkview.flights2.flight.FlightStatus;
import org.junit.Test;

import java.io.IOException;

public class Test_Pilot1309680_1_60_EKCHtoUnfinished_DB extends DbBaseTest {

    @Test
    public void test() throws IOException {
        initCsvSnapshot("/snapshots/pilot-1309680_from-1_amount-60.csv");
        initDBPersistence();
        doTest(1309680, 1, 60);
    }

    public void report_1_6() throws IOException {
        checkDBNoPilotContext();
    }

    public void report_7_18() throws IOException {
        checkDBFlightsCount(1);
        checkDBCurrFlightStatus(FlightStatus.Departure);
        checkDBCurrFlightLastSeen("EKCH");
        checkDBCurrFlightRoute(null, null);

        doReset();
    }

    public void report_19_37() throws IOException {
        checkDBFlightsCount(1);
        checkDBCurrFlightStatus(FlightStatus.Flying);
        checkDBCurrFlightLastSeen(null);
        checkDBCurrFlightRoute("EKCH", null);
        checkDBCurrFlightCallsign("DAL21");

        doReset();
    }

    public void report_37() throws IOException {
        checkDBFlightsCount(1);
    }

    public void report_38_60() throws IOException {
        checkDBFlightsCount(1);
        checkDBCurrFlightStatus(FlightStatus.Lost);
        checkDBCurrFlightRoute("EKCH", null);

        doReset();
    }
}
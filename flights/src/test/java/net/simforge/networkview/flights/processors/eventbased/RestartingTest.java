package net.simforge.networkview.flights.processors.eventbased;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.commons.legacy.BM;
import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.networkview.flights.processors.eventbased.datasource.CsvDatasource;
import net.simforge.networkview.flights.processors.eventbased.datasource.ReportDatasource;
import net.simforge.networkview.flights.processors.eventbased.persistence.DBFlight;
import net.simforge.networkview.flights.processors.eventbased.persistence.DBPersistenceLayer;
import net.simforge.networkview.flights.processors.eventbased.persistence.DBPilotStatus;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RestartingTest {
    private int pilotNumber = 1261420;

    @Test
    public void test() throws IOException {
        InputStream is = Class.class.getResourceAsStream("/snapshots/pilot-1261420_from-670000_amount-10000.csv");
        String csvContent = IOHelper.readInputStream(is);
        ReportDatasource reportDatasource = new CsvDatasource(Csv.fromContent(csvContent));

        SessionFactory flightsDb1 = Flights.buildSessionFactoryWithSchema("flights-testdb1");
        SessionFactory flightsDb2 = Flights.buildSessionFactoryWithSchema("flights-testdb2");

        RecognitionContext recognitionContext1 = new RecognitionContext(reportDatasource,
                new DBPersistenceLayer(flightsDb1, reportDatasource),
                null);

        RecognitionContext recognitionContext2 = null;
        Report lastProcessedReport2 = null;
        boolean timeToReset2 = true;
        for (int i = 0; i < 10000; i++) {
            if (recognitionContext2 == null || timeToReset2) {
                recognitionContext2 = new RecognitionContext(reportDatasource,
                        new DBPersistenceLayer(flightsDb2, reportDatasource),
                        lastProcessedReport2);
                recognitionContext2.loadActivePilotContexts();
                // timeToReset2 = false; -- it will be set to actual state in bottom of cycle
            }

            recognitionContext1.processNextReport();

            recognitionContext2.processNextReport();
            lastProcessedReport2 = recognitionContext2.getLastProcessedReport();

            compareDatabases(flightsDb1, flightsDb2);

            timeToReset2 = (i % 500) == 0 && i != 0;
        }
    }

    private void compareDatabases(SessionFactory flightsDb1, SessionFactory flightsDb2) {
        DBPilotStatus ps1 = loadPilotStatus(flightsDb1, pilotNumber);
        DBPilotStatus ps2 = loadPilotStatus(flightsDb2, pilotNumber);

        if (ps1 == null) {
            assertNull(ps2);
            return;
        }

        // assertEquals(ps1.getLastProcessedReportId(), ps2.getLastProcessedReportId());

        DBFlight flight1 = ps1.getCurrFlight();
        DBFlight flight2 = ps2.getCurrFlight();

        if (flight1 == null) {
            assertNull(flight2);
        } else {
            assertEquals(flight1.getFirstSeenReportId(), flight2.getFirstSeenReportId());
            assertEquals(flight1.getLastSeenReportId(), flight2.getLastSeenReportId());
            assertEquals(flight1.getTakeoffReportId(), flight2.getTakeoffReportId());
            assertEquals(flight1.getLandingReportId(), flight2.getLandingReportId());
        }

        int flightCount1 = loadFlightCount(flightsDb1, pilotNumber);
        int flightCount2 = loadFlightCount(flightsDb2, pilotNumber);

        assertEquals(flightCount1, flightCount2);
    }

    private int loadFlightCount(SessionFactory flightsDb, int pilotNumber) {
        BM.start("RestartingTest.loadFlightCount");
        try (Session session = flightsDb.openSession()) {

            //noinspection JpaQlInspection
            Long result = (Long) session
                    .createQuery("select count(f) from Flight f where pilotNumber = :pilotNumber")
                    .setInteger("pilotNumber", pilotNumber)
                    .uniqueResult();

            return result.intValue();

        } finally {
            BM.stop();
        }
    }

    private DBPilotStatus loadPilotStatus(SessionFactory flightsDb, int pilotNumber) {
        BM.start("RestartingTest.loadPilotStatus");
        try (Session session = flightsDb.openSession()) {

            //noinspection JpaQlInspection
            return (DBPilotStatus) session
                    .createQuery("select ps from PilotStatus ps where pilotNumber = :pilotNumber")
                    .setInteger("pilotNumber", pilotNumber)
                    .uniqueResult();

        } finally {
            BM.stop();
        }
    }
}

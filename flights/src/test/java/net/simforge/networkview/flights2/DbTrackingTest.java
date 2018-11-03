package net.simforge.networkview.flights2;

import net.simforge.commons.hibernate.SessionFactoryBuilder;
import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.flights.datasource.CsvDatasource;
import net.simforge.networkview.flights.datasource.ReportDatasource;
import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.flight.FlightStatus;
import net.simforge.networkview.flights2.persistence.DBFlight;
import net.simforge.networkview.flights2.persistence.DBPersistenceLayer;
import net.simforge.networkview.flights2.persistence.DBPilotStatus;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DbTrackingTest {
    private static final Logger logger = LoggerFactory.getLogger(DbTrackingTest.class.getName());

    private SessionFactory flightsSessionFactory;
    private PersistenceLayer persistenceLayer;
    private Report report;
    private int pilotNumber = 1309680;

    @Test
    public void test() throws IOException {
        flightsSessionFactory = SessionFactoryBuilder
                .forDatabase("flights")
                .entities(new Class[]{DBPilotStatus.class, DBFlight.class})
                .createSchemaIfNeeded()
                .build();

        ReportDatasource reportDatasource = loadReportDatasource();
        persistenceLayer = new DBPersistenceLayer(flightsSessionFactory, reportDatasource);

        for (int i = 1; i <= 60; i++) {
            MainContext mainContext = new MainContext(reportDatasource, persistenceLayer);

            mainContext.setLastReport(report);
            mainContext.loadActivePilotContexts();

            mainContext.processReports(1);
            report = mainContext.getLastReport();

            invokeSingleReportCheckMethod();
            invokeRangeReportCheckMethods();
        }
    }

    private void invokeSingleReportCheckMethod() {
        invokeCheckMethod("report_" + report.getId());
    }

    private void invokeRangeReportCheckMethods() {
        Method[] methods = getClass().getMethods();
        for (Method method : methods) {
            String methodName = method.getName();

            String[] strings = methodName.split("_");

            if (strings.length != 3) {
                continue;
            }

            if (!strings[0].equals("report")) {
                continue;
            }

            int fromReportId = Integer.parseInt(strings[1]);
            int toReportId = Integer.parseInt(strings[2]);

            if (fromReportId <= report.getId() && report.getId() <= toReportId) {
                invokeCheckMethod(methodName);
            }
        }
    }

    private void invokeCheckMethod(String checkMethodName) {
        try {
            Method method = getClass().getMethod(checkMethodName);
            method.invoke(this);
            logger.info("Report " + report.getId() + ": checked, check method " + checkMethodName);
        } catch (NoSuchMethodException e) {
            //logger.info("No checks for report " + report.getId());
        } catch (IllegalAccessException e) {
            logger.info("Can't access to checks for report " + report.getId() + ", check method " + checkMethodName);
        } catch (InvocationTargetException e) {
            Throwable throwable = e.getTargetException();
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else if (throwable instanceof Error) {
                throw (Error) throwable;
            } else {
                logger.error("Exception during checks for report " + report.getId(), e);
            }
        }
    }

    private CsvDatasource loadReportDatasource() throws IOException {
        InputStream is = Class.class.getResourceAsStream("/snapshots/pilot-1309680_from-1_amount-60.csv");
        String csvContent = IOHelper.readInputStream(is);
        return new CsvDatasource(Csv.fromContent(csvContent));
    }

    public void report_1_6() throws IOException {
        checkNoPilotContext();
    }

    public void report_7_18() throws IOException {
        checkFlightsCount(1);
        checkCurrFlightStatus(FlightStatus.Departure);
        checkCurrFlightRoute("EKCH", null);
    }

    public void report_19_37() throws IOException {
        checkFlightsCount(1);
        checkCurrFlightStatus(FlightStatus.Flying);
        checkCurrFlightRoute("EKCH", null);
    }

    public void report_37() throws IOException {
        checkFlightsCount(1);
    }

    public void report_38_60() throws IOException {
        checkFlightsCount(1);
        checkCurrFlightStatus(FlightStatus.Lost);
        checkCurrFlightRoute("EKCH", null);
    }




    private void checkNoPilotContext() throws IOException {
        assertNull(persistenceLayer.loadContext(pilotNumber));
    }

    private void checkFlightsCount(int expectedCount) {
        try (Session session = flightsSessionFactory.openSession()) {
            //noinspection JpaQlInspection
            Long actualCount = (Long) session
                    .createQuery("select count(f) from Flight f where pilotNumber = :pilotNumber")
                    .setInteger("pilotNumber", pilotNumber)
                    .uniqueResult();
            assertEquals(expectedCount, actualCount.intValue());
        }
    }

    private void checkCurrFlightStatus(FlightStatus expectedStatus) throws IOException {
        PilotContext pilotContext = persistenceLayer.loadContext(pilotNumber);
        Flight currFlight = pilotContext.getCurrFlight();
        assertNotNull(currFlight);
        assertEquals(expectedStatus, currFlight.getStatus());
    }

    private void checkCurrFlightRoute(String expectedFrom, String expectedTo) throws IOException {
        PilotContext pilotContext = persistenceLayer.loadContext(pilotNumber);
        Flight currFlight = pilotContext.getCurrFlight();
        assertNotNull(currFlight);

        if (expectedFrom != null) {
            assertEquals(expectedFrom, currFlight.getOrigin().getAirportIcao());
        }

        if (expectedTo != null) {
            assertEquals(expectedTo, currFlight.getDestination().getAirportIcao());
        } else {
            assertNull(currFlight.getDestination());
        }
    }
}

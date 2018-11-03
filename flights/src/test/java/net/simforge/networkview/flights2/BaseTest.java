package net.simforge.networkview.flights2;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.commons.misc.Misc;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.flights.datasource.CsvDatasource;
import net.simforge.networkview.flights.datasource.ReportDatasource;
import net.simforge.networkview.flights2.events.FlightStatusEvent;
import net.simforge.networkview.flights2.events.PilotKnownPositionEvent;
import net.simforge.networkview.flights2.events.PilotUnknownPositionEvent;
import net.simforge.networkview.flights2.events.TrackingEvent;
import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.flight.FlightStatus;
import net.simforge.networkview.flights2.flight.Flightplan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

public abstract class BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class.getName());

    protected ReportDatasource reportDatasource;
    protected PersistenceLayer persistenceLayer;
    private MainContext mainContext;

    protected int pilotNumber;

    private boolean needReset = false;

    private Report report;
    private PilotContext pilotContext;
    private Flight flight;

    private Map<Long, List<TrackingEvent>> eventHistory = new HashMap<>();

    private int checksDone;


    protected void doTest(int pilotNumber, int fromReportId, int toReportId) throws IOException {
        this.pilotNumber = pilotNumber;

        report = reportDatasource.loadReport(fromReportId);
        pilotContext = null;
        flight = null;

        needReset = true;

        checksDone = 0;

        int reportsToProcess = toReportId - fromReportId + 1;

        for (int i = 0; i < reportsToProcess; i++) {
            if (needReset) {
                mainContext = new MainContext(reportDatasource, persistenceLayer);
                mainContext.setLastReport(report);
                mainContext.loadActivePilotContexts();
                needReset = false;
            }

            mainContext.processReports(1);

            report = mainContext.getLastReport();
            pilotContext = mainContext.getPilotContext(pilotNumber);
            if (pilotContext != null) {
                flight = pilotContext.getCurrFlight();
                eventHistory.put(this.report.getId(), pilotContext.getRecentEvents());
            } else {
                flight = null;
            }

            invokeSingleReportCheckMethod();
            invokeRangeReportCheckMethods();
        }

        if (checksDone == 0) {
            fail("No any check method invoked");
        } else {
            logger.info("Test finished: {} checks done", checksDone);
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

    protected void initCsvSnapshot(String filename) throws IOException {
        InputStream is = Class.class.getResourceAsStream(filename);
        String csvContent = IOHelper.readInputStream(is);
        reportDatasource = new CsvDatasource(Csv.fromContent(csvContent));
    }

    protected void initNoOpPersistence() {
        persistenceLayer = new PersistenceLayer() {
            @Override
            public List<PilotContext> loadActivePilotContexts(LocalDateTime lastProcessedReportDt) {
                return Collections.EMPTY_LIST;
            }

            @Override
            public PilotContext createContext(int pilotNumber, Report seenReport) {
                return new PilotContext(pilotNumber);
            }

            @Override
            public PilotContext loadContext(int pilotNumber) {
                return null; // we do not save flights and we do not load contexts because of that
            }

            @Override
            public PilotContext saveChanges(PilotContext pilotContext) {
                return pilotContext; // we do not save anything
            }
        };
    }

    protected void countCheckMethod() {
        checksDone++;
    }

    // === Event checks ================================================================================================

    private List<TrackingEvent> _getEvents(long reportId) {
        List<TrackingEvent> events = eventHistory.get(reportId);
        if (events == null) {
            return Collections.emptyList();
        }

        return events.stream().filter(
                event -> !(event instanceof PilotKnownPositionEvent
                        || event instanceof PilotUnknownPositionEvent)
        ).collect(Collectors.toList());
    }

    private void checkEvent(String eventType) {
        List<TrackingEvent> events = _getEvents(report.getId());
        for (TrackingEvent event : events) {
            if (eventType.equals(event.getType())) {
                logger.info(String.format("\tOK Event '%s'", eventType));
                return;
            }
        }
        fail(String.format("No '%s' event found", eventType));
    }

    protected void checkNoEvents() {
        countCheckMethod();

        List<TrackingEvent> events = _getEvents(report.getId());
        if (events.isEmpty()) {
            logger.info("\tOK No events");
        } else {
            fail("No events check failed");
        }
    }

    protected void checkNoPilotContext() {
        countCheckMethod();

        assertNull(pilotContext);
        logger.info("\tOK No Pilot Context");
    }

    protected void checkPositionKnown() {
        countCheckMethod();

        assertTrue(pilotContext.getCurrPosition().isPositionKnown());
        logger.info("\tOK Position Known");
    }

    protected void checkPositionUnknown() {
        countCheckMethod();

        assertFalse(pilotContext.getCurrPosition().isPositionKnown());
        logger.info("\tOK Position Unknown");
    }

    protected void checkOnGround() {
        countCheckMethod();

        assertTrue(pilotContext.getCurrPosition().isOnGround());
        logger.info("\tOK On Ground");
    }

    protected void checkFlying() {
        countCheckMethod();

        assertTrue(!pilotContext.getCurrPosition().isOnGround());
        logger.info("\tOK Flying");
    }

    protected void checkOnlineEvent() {
        countCheckMethod();

        checkEvent("pilot/online");
    }

    protected void checkOfflineEvent() {
        countCheckMethod();

        checkEvent("pilot/offline");
    }

    protected void checkTakeoffEvent() {
        countCheckMethod();

        checkEvent("pilot/takeoff");
    }

    protected void checkLandingEvent() {
        countCheckMethod();

        checkEvent("pilot/landing");
    }

    // === Flight checks ===============================================================================================
    protected void checkFlight() {
        countCheckMethod();

        assertNotNull(flight);
        logger.info("\tOK Flight");
    }

    protected void checkNoFlight() {
        countCheckMethod();

        assertNull(flight);
        logger.info("\tOK No flight");
    }

    protected void checkFlightStatus(FlightStatus status) {
        countCheckMethod();

        checkFlight();
        assertEquals(status, flight.getStatus());
        logger.info(String.format("\tOK Flight status: %s", status));
    }

    protected void checkFlightRoute(String origin, String destination) {
        countCheckMethod();

        checkFlight();
        checkFlightRoute(flight, origin, destination);
    }

    protected void checkFlightRoute(Flight flight, String expectedDeparture, String expectedDestination) {
        countCheckMethod();

        assertEquals(expectedDeparture, flight.getDeparture() != null ? flight.getDeparture().getAirportIcao() : null);
        assertEquals(expectedDestination, flight.getDestination() != null ? flight.getDestination().getAirportIcao() : null);
        logger.info(String.format("\tOK Flight route: %s-%s", Misc.mn(expectedDeparture, "[--]"), Misc.mn(expectedDestination, "[--]")));
    }

    protected void checkFlightStatusEvent(FlightStatus status) {
        countCheckMethod();

        String eventType = "flight/status/" + status.toString();
        checkEvent(eventType);
    }

    protected void checkFlightplanEvent() {
        countCheckMethod();

        checkEvent("flight/flightplan");
    }

    protected void checkFlightplanData(String fpAircraft, String fpDep, String fpDest) {
        countCheckMethod();

        checkFlight();
        checkFlightplanData(flight, fpAircraft, fpDep, fpDest);
    }

    protected void checkFlightplanData(Flight flight, String fpAircraftType, String fpDeparture, String fpDestination) {
        countCheckMethod();

        Flightplan flightplan = flight.getFlightplan();
        assertNotNull(flightplan);
        assertEquals(fpAircraftType, flightplan.getAircraftType());
        assertEquals(fpDeparture, flightplan.getDeparture());
        assertEquals(fpDestination, flightplan.getDestination());
        logger.info(String.format("\tOK Flightplan: %s, %s-%s", fpAircraftType, fpDeparture, fpDestination));
    }

    protected void checkCallsign(String callsign) {
        countCheckMethod();

        Flightplan flightplan = flight.getFlightplan();
        assertNotNull(flightplan);
        assertEquals(callsign, flightplan.getCallsign());
        logger.info("\tOK Callsign");
    }

    private Flight getFlightFromStatusEvent(FlightStatus status) {
        List<TrackingEvent> events = _getEvents(report.getId());
        String eventType = "flight/status/" + status.toString();
        FlightStatusEvent foundEvent = null;
        for (TrackingEvent event : events) {
            if (eventType.equals(event.getType())) {
                foundEvent = (FlightStatusEvent) event;
                break;
            }
        }

        assertNotNull(foundEvent);
        return foundEvent.getFlight();
    }
}

package net.simforge.networkview.flights;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.commons.misc.Misc;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.flights.datasource.CsvDatasource;
import net.simforge.networkview.flights.datasource.ReportDatasource;
import net.simforge.networkview.flights.events.TrackingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.*;

@SuppressWarnings("WeakerAccess")
public class BaseTest {

    protected static final String ON_GROUND = "[ON_GROUND]";

    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class.getName());

    protected ReportDatasource reportDatasource;
    protected PersistenceLayer persistenceLayer;
    private RecognitionContext mainContext;

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
                mainContext = new RecognitionContext(reportDatasource, persistenceLayer, report);
                mainContext.loadActivePilotContexts();
                pilotContext = null;
                flight = null;
                needReset = false;
            }

            mainContext.processNextReport();

            this.report = mainContext.getLastProcessedReport();
            pilotContext = mainContext.getPilotContext(pilotNumber);
            if (pilotContext != null) {
                flight = pilotContext.getCurrFlight();

                List<TrackingEvent> events = new LinkedList<>(pilotContext.getRecentEvents());
                if (flight != null) {
                    events.addAll(flight.getRecentEvents());
                }
                pilotContext.getRecentFlights().forEach(f -> events.addAll(f.getRecentEvents()));

                eventHistory.put(this.report.getId(), events);
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
                return new ArrayList<>();
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

    protected void doReset() {
        needReset = true;
    }

    // === Event checks ================================================================================================

    private List<TrackingEvent> _getEvents(long reportId) {
        List<TrackingEvent> events = eventHistory.get(reportId);
        if (events == null) {
            return Collections.emptyList();
        }

        return events;
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

    protected void checkNoPilotContextOrPositionUnknown() {
        countCheckMethod();

        if (pilotContext != null) {
            checkPositionUnknown();
        }
        logger.info("\tOK No Pilot Context Or Position Unknown");
    }

    protected void checkPositionKnown() {
        countCheckMethod();

        assertTrue(pilotContext.getLastProcessedPosition().isPositionKnown());
        logger.info("\tOK Position Known");
    }

    protected void checkPositionUnknown() {
        countCheckMethod();

        assertFalse(pilotContext.getLastProcessedPosition().isPositionKnown());
        logger.info("\tOK Position Unknown");
    }

    protected void checkOnGround() {
        countCheckMethod();

        assertTrue(pilotContext.getLastProcessedPosition().isOnGround());
        logger.info("\tOK On Ground");
    }

    protected void checkFlying() {
        countCheckMethod();

        assertFalse(pilotContext.getLastProcessedPosition().isOnGround());
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

    protected void checkFlightStatus(FlightStatus expectedStatus) {
        countCheckMethod();

        checkFlight();
        assertTrue(flight.getStatus().is(expectedStatus));
        logger.info(String.format("\tOK Flight status: %s", expectedStatus));
    }

    protected void checkFlightRoute(String expectedTakeoff, String expectedLanding) {
        countCheckMethod();

        checkFlight();
        checkFlightRoute(flight, expectedTakeoff, expectedLanding);
    }

    protected void checkFlightRoute(Flight flight, String expectedTakeoff, String expectedLanding) {
        countCheckMethod();

        if (Objects.equals(expectedTakeoff, ON_GROUND)) {
            assertNotNull(flight.getTakeoff());
        } else if (expectedTakeoff != null) {
            assertEquals(expectedTakeoff, flight.getTakeoff() != null ? flight.getTakeoff().getAirportIcao() : null);
        } else {
            assertNull(flight.getTakeoff());
        }
        if (Objects.equals(expectedLanding, ON_GROUND)) {
            assertNotNull(flight.getLanding());
        } else if (expectedLanding != null) {
            assertEquals(expectedLanding, flight.getLanding() != null ? flight.getLanding().getAirportIcao() : null);
        } else {
            assertNull(flight.getLanding());
        }
        logger.info(String.format("\tOK Flight route: %s-%s", Misc.mn(expectedTakeoff, "[--]"), Misc.mn(expectedLanding, "[--]")));
    }

    protected void checkFlightLastSeenIcao(String expectedLastSeenIcao) {
        countCheckMethod();

        checkFlightLastSeenIcao(flight, expectedLastSeenIcao);
    }

    protected void checkFlightLastSeenIcao(Flight flight, String expectedLastSeenIcao) {
        countCheckMethod();

        assertEquals(expectedLastSeenIcao, flight.getLastSeen().getAirportIcao());
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

        assertEquals(callsign, flight.getCallsign());
        logger.info("\tOK Callsign");
    }

    protected Flight getFlightFromStatusEvent(FlightStatus status) {
        if (flight != null && hasEvent(flight, status)) {
            return flight;
        }

        return pilotContext.getRecentFlights().stream().filter(f -> hasEvent(f, status)).findFirst().orElse(null);
    }

    private boolean hasEvent(Flight flight, FlightStatus status) {
        List<TrackingEvent> events = flight.getRecentEvents();
        String eventType = "flight/status/" + status.toString();
        for (TrackingEvent event : events) {
            if (eventType.equals(event.getType())) {
                return true;
            }
        }
        return false;
    }

    protected void checkRecentFlightCount(int expectedFlightCount) {
        countCheckMethod();

        assertEquals(expectedFlightCount, pilotContext.getRecentFlights().size());
    }
}

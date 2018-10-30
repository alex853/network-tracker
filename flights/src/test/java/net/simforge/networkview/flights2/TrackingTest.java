package net.simforge.networkview.flights2;

import junit.framework.TestCase;
import net.simforge.commons.misc.Misc;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.flights.datasource.ReportDatasource;
import net.simforge.networkview.flights.model.Flightplan;
import net.simforge.networkview.flights2.events.FlightStatusEvent;
import net.simforge.networkview.flights2.events.PilotKnownPositionEvent;
import net.simforge.networkview.flights2.events.PilotUnknownPositionEvent;
import net.simforge.networkview.flights2.events.TrackingEvent;
import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.flight.FlightStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class TrackingTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(TrackingTest.class.getName());

    private int pilotNumber;
    private int fromReportId;
    private int toReportId;

    private ReportDatasource reportDatasource;

    protected PilotContext pilotContext;
    private Map<Long, List<TrackingEvent>> pilotEventHistory = new HashMap<>();

    protected Report report;
    //    protected ReportPilotPosition position;
    protected Flight flight;

    protected void setDatasource(ReportDatasource reportDatasource) {
        this.reportDatasource = reportDatasource;
    }

    protected void init(int pilotNumber, int fromReportId, int toReportId) {
        this.pilotNumber = pilotNumber;
        this.fromReportId = fromReportId;
        this.toReportId = toReportId;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void testTracking() throws IOException {
        PersistenceLayer persistenceLayer = new NoOpPersistenceLayer();

        MainContext mainContext = new MainContext(reportDatasource, persistenceLayer);

        int reportsToProcess = toReportId - fromReportId + 1;

        for (int i = 0; i < reportsToProcess; i++) {
            mainContext.processReports(1);

            report = mainContext.getLastReport();

            pilotContext = mainContext.getPilotContext(pilotNumber);
            if (pilotContext != null) {
                flight = pilotContext.getCurrFlight();
                pilotEventHistory.put(report.getId(), pilotContext.getRecentEvents());
            }

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

    private List<TrackingEvent> pilotContext_getEvents(long reportId) {
        List<TrackingEvent> events = pilotEventHistory.get(reportId);
        if (events == null) {
            return Collections.emptyList();
        }

        return events.stream().filter(
                event -> !(event instanceof PilotKnownPositionEvent
                        || event instanceof PilotUnknownPositionEvent)
        ).collect(Collectors.toList());
    }

    protected void checkEvent(String eventType) {
        List<TrackingEvent> events = pilotContext_getEvents(report.getId());
        for (TrackingEvent event : events) {
            if (eventType.equals(event.getType())) {
                logger.info(String.format("\tOK Event '%s'", eventType));
                return;
            }
        }
        fail(String.format("No '%s' event found", eventType));
    }

    protected void checkNoEvents() {
        List<TrackingEvent> events = pilotContext_getEvents(report.getId());
        if (events.isEmpty()) {
            logger.info("\tOK No events");
        } else {
            fail("No events check failed");
        }
    }

    protected void checkNoPilotContext() {
        assertNull(pilotContext);
        logger.info("\tOK No Pilot Context");
    }

    protected void checkPositionKnown() {
        assertTrue(pilotContext.getCurrPosition().isPositionKnown());
        logger.info("\tOK Position Known");
    }

    protected void checkPositionUnknown() {
        assertFalse(pilotContext.getCurrPosition().isPositionKnown());
        logger.info("\tOK Position Unknown");
    }

    protected void checkOnGround() {
        assertTrue(pilotContext.getCurrPosition().isOnGround());
        logger.info("\tOK On Ground");
    }

    protected void checkFlying() {
        assertTrue(!pilotContext.getCurrPosition().isOnGround());
        logger.info("\tOK Flying");
    }

    protected void checkOnlineEvent() {
        checkEvent("pilot/online");
    }

    protected void checkOfflineEvent() {
        checkEvent("pilot/offline");
    }

    protected void checkTakeoffEvent() {
        checkEvent("pilot/takeoff");
    }

    protected void checkLandingEvent() {
        checkEvent("pilot/landing");
    }

    // === Flight checks ===============================================================================================
    protected void checkFlight() {
        assertNotNull(flight);
        logger.info("\tOK Flight");
    }

    protected void checkNoFlight() {
        assertNull(flight);
        logger.info("\tOK No flight");
    }

    protected void checkFlightStatus(FlightStatus status) {
        checkFlight();
        assertEquals(status, flight.getStatus());
        logger.info(String.format("\tOK Flight status: %s", status));
    }

    protected void checkFlightRoute(String origin, String destination) {
        checkFlight();
        checkFlightRoute(flight, origin, destination);
    }

    protected void checkFlightRoute(Flight flight, String origin, String destination) {
        assertEquals(origin, flight.getOrigin().getAirportIcao());
        assertEquals(destination, flight.getDestination().getAirportIcao());
        logger.info(String.format("\tOK Flight route: %s-%s", Misc.mn(origin, "[--]"), Misc.mn(destination, "[--]")));
    }

    protected void checkFlightFlightplanEvent() {
        checkEvent("flight/flightplan");
    }

    protected void checkFlightStatusEvent(FlightStatus status) {
        String eventType = "flight/status/" + status.toString();
        checkEvent(eventType);
    }

    protected void checkFlightFlightplanData(String fpAircraft, String fpDep, String fpDest) {
        checkFlight();
        checkFlightFlightplanData(flight, fpAircraft, fpDep, fpDest);
    }

    protected void checkFlightFlightplanData(Flight flight, String fpAircraft, String fpDep, String fpDest) {
        Flightplan flightplan = flight.getFlightplan();
        assertNotNull(flightplan);
        assertEquals(fpAircraft, flightplan.getAircraft());
        assertEquals(fpDep, flightplan.getOrigin());
        assertEquals(fpDest, flightplan.getDestination());
        logger.info(String.format("\tOK Flightplan: %s, %s-%s", fpAircraft, fpDep, fpDest));
    }

    protected Flight getFlightFromStatusEvent(FlightStatus status) {
        List<TrackingEvent> events = pilotContext_getEvents(report.getId());
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

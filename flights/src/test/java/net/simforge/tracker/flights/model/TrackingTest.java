package net.simforge.tracker.flights.model;

import junit.framework.TestCase;
import net.simforge.commons.misc.Misc;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.tracker.flights.datasource.ReportDatasource;
import net.simforge.tracker.flights.model.events.FlightStatusEvent;
import net.simforge.tracker.flights.model.events.TrackingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public abstract class TrackingTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(MainContext.class.getName());

    private int pilotNumber;
    private int fromReportId;
    private int toReportId;

    private ReportDatasource reportDatasource;
    private MainContext.Strategy strategy;

    protected PilotContext pilotContext;

    protected Report report;
//    protected ReportPilotPosition position;
    protected Flight flight;

    protected void setDatasource(ReportDatasource reportDatasource) {
        this.reportDatasource = reportDatasource;
    }

    public void setStrategy(MainContext.Strategy strategy) {
        this.strategy = strategy;
    }

    protected void init(int pilotNumber, int fromReportId, int toReportId) {
        this.pilotNumber = pilotNumber;
        this.fromReportId = fromReportId;
        this.toReportId = toReportId;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void testTracking() throws IOException {
        MainContext mainContext = new MainContext();
        mainContext.setReportDatasource(reportDatasource);
        mainContext.setStrategy(new MainContext.Strategy() {
            @Override
            public void initPilotContext(PilotContext pilotContext, ReportPilotPosition pilotPosition) {
                if (strategy != null) {
                    strategy.initPilotContext(pilotContext, pilotPosition);
                }
            }

            @Override
            public void onPilotContextProcessed(PilotContext pilotContext) {
                if (strategy != null) {
                    strategy.onPilotContextProcessed(pilotContext);
                }

                TrackingTest.this.pilotContext = pilotContext;

                try {
                    report = reportDatasource.loadReport(TrackingTest.this.pilotContext.getPosition().getReportId());
                } catch (IOException e) {
                    throw new UnsupportedOperationException(e);
                }

                flight = pilotContext.getCurrentFlight();

                invokeSingleReportCheckMethod();
                invokeRangeReportCheckMethods();
            }
        });
        mainContext.processReports(toReportId - fromReportId + 1);
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

    protected void checkEvent(String eventType) {
        List<TrackingEvent> events = pilotContext.getEvents(report.getId());
        for (TrackingEvent event : events) {
            if (eventType.equals(event.getType())) {
                logger.info(String.format("\tOK Event '%s'", eventType));
                return;
            }
        }
        fail(String.format("No '%s' event found", eventType));
    }

    protected void checkNoEvents() {
        List<TrackingEvent> events = pilotContext.getEvents(report.getId());
        if (events.isEmpty()) {
            logger.info("\tOK No events");
        } else {
            fail("No events check failed");
        }
    }

    protected void checkPositionKnown() {
        assertTrue(pilotContext.getPosition().isPositionKnown());
        logger.info("\tOK Position Known");
    }

    protected void checkPositionUnknown() {
        assertFalse(pilotContext.getPosition().isPositionKnown());
        logger.info("\tOK Position Unknown");
    }

    protected void checkOnGround() {
        assertTrue(pilotContext.getPosition().isOnGround());
        logger.info("\tOK On Ground");
    }

    protected void checkFlying() {
        assertTrue(!pilotContext.getPosition().isOnGround());
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

    // === Movement checks =============================================================================================
    protected void checkMovement() {
        assertNotNull(flight);
        logger.info("\tOK Movement");
    }

    protected void checkNoMovement() {
        assertNull(flight);
        logger.info("\tOK No flight");
    }

    protected void checkMovementStatus(FlightStatus status) {
        checkMovement();
        assertEquals(status, flight.getStatus());
        logger.info(String.format("\tOK Movement status: %s", status));
    }

    protected void checkMovementRoute(String origin, String destination) {
        checkMovement();
        checkMovementRoute(flight, origin, destination);
    }

    protected void checkMovementRoute(Flight movement, String origin, String destination) {
        assertEquals(origin, movement.getOriginIcao());
        assertEquals(destination, movement.getDestinationIcao());
        logger.info(String.format("\tOK Movement route: %s-%s", Misc.mn(origin, "[--]"), Misc.mn(destination, "[--]")));
    }

    protected void checkMovementFlightplanEvent() {
        checkEvent("flight/flightplan");
    }

    protected void checkMovementStatusEvent(FlightStatus status) {
        String eventType = "flight/status/" + status.toString();
        checkEvent(eventType);
    }

    protected void checkMovementFlightplanData(String fpAircraft, String fpDep, String fpDest) {
        checkMovement();
        checkMovementFlightplanData(flight, fpAircraft, fpDep, fpDest);
    }

    protected void checkMovementFlightplanData(Flight movement, String fpAircraft, String fpDep, String fpDest) {
        Flightplan flightplan = movement.getFlightplan();
        assertNotNull(flightplan);
        assertEquals(fpAircraft, flightplan.getAircraft());
        assertEquals(fpDep, flightplan.getOrigin());
        assertEquals(fpDest, flightplan.getDestination());
        logger.info(String.format("\tOK Flightplan: %s, %s-%s", fpAircraft, fpDep, fpDest));
    }

    protected Flight getMovementFromStatusEvent(FlightStatus status) {
        List<TrackingEvent> events = pilotContext.getEvents(report.getId());
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

package net.simforge.ot.analyzers;

import forge.commons.runner.Runner;
import forge.commons.db.DB;
import net.simforge.commons.misc.Marker;
import net.simforge.commons.misc.Misc;
import net.simforge.commons.persistence.Persistence;
import net.simforge.commons.logging.LogHelper;

import java.sql.SQLException;
import java.sql.Connection;
import java.util.logging.Logger;
import java.util.*;

import entities.*;
import core.PilotPosition;
import org.joda.time.DateTime;
import tools.Tools;

public class Tracking {
    private static final Logger log = LogHelper.getLogger("tracking");

    public static void main(String[] args) throws SQLException {
        Runner.start("Tracking");

        Marker lastProcessedReportId = new Marker("Tracking");

        //noinspection InfiniteLoopStatement
        while (true) {
            Report report;

            int lastReportId = lastProcessedReportId.get();
            if (lastReportId == -1) {
                report = net.simforge.ot.db.DBOps.live().loadFirstReport(true);
            } else {
                report = net.simforge.ot.db.DBOps.live().loadNextReport(lastReportId, true);
            }

            if (report == null) {
                Misc.sleep(60000);
                continue;
            }

            process(report);

            lastProcessedReportId.set(report.getId());

            Misc.sleep(100);
        }
    }

    private static void process(Report report) throws SQLException {
        log.info("Report: "  + Misc.yMdHms.print(report.getReportDt()) + " REP: " + report.getId());

        Report previous = getPreviousReport(report);
        if (previous == null) {
            log.info("    There's no previous report - GAP? skipping");
            return;
        }

        List<ReportPilotPosition> currentPositions = net.simforge.ot.db.DBOps.live().loadPilotPositions(report);
        List<ReportPilotPosition> previousPositions = net.simforge.ot.db.DBOps.live().loadPilotPositions(previous);

        Map<Integer, ReportPilotPosition> pilot2current = list2map(currentPositions);
        Map<Integer, ReportPilotPosition> pilot2previous = list2map(previousPositions);

        Set<Integer> wentOnline = new TreeSet<Integer>(pilot2current.keySet());
        wentOnline.removeAll(pilot2previous.keySet());

        Set<Integer> online = new TreeSet<Integer>(pilot2current.keySet());
        online.removeAll(wentOnline);

        for (Integer pilotNumber : online) {
            processOnlinePilot(pilotNumber, report.getDt(),
                    pilot2current.get(pilotNumber),
                    pilot2previous.get(pilotNumber));
        }
    }

    private static Report getPreviousReport(Report current) throws SQLException {
        Report previous = net.simforge.ot.db.DBOps.live().loadPreviousReport(current.getId(), true);
        if (previous == null) {
            return null;
        }
        if (Tools.isGap(current, previous)) {
            return null;
        }
        return previous;
    }

    private static Map<Integer, ReportPilotPosition> list2map(List<ReportPilotPosition> positions) {
        Map<Integer, ReportPilotPosition> map = new HashMap<Integer, ReportPilotPosition>();
        for (ReportPilotPosition position : positions) {
            map.put(position.getPilotNumber(), position);
        }
        return map;
    }

    private static void processOnlinePilot(Integer pilotNumber, DateTime dt, ReportPilotPosition current, ReportPilotPosition previous) throws SQLException {
        PilotPosition currentPP = new PilotPosition(current);
        PilotPosition previousPP = new PilotPosition(previous);

        TrackingEvent.PositionState currentState = getPositionState(currentPP);
        TrackingEvent.PositionState previousState = getPositionState(previousPP);

        TrackingEvent event = null;
        if (currentState == TrackingEvent.PositionState.Flying && previousState == TrackingEvent.PositionState.InAirport) {
            event = new TrackingEvent();
            event.setEventType(TrackingEvent.EventType.Takeoff);
        } else if (currentState == TrackingEvent.PositionState.InAirport && previousState == TrackingEvent.PositionState.Flying) {
            event = new TrackingEvent();
            event.setEventType(TrackingEvent.EventType.Landing);
        }

        if (event != null) {
            event.setPilotNumber(pilotNumber);
            event.setDt(dt);
            event.setCallsign(current.getCallsign());
            event.setPositionState(currentState);
            event.setPositionIcao(currentPP.getNearestAirport() != null ? currentPP.getNearestAirport().getAirport().getIcao() : null);
            event.setLatitude(current.getLatitude());
            event.setLongitude(current.getLongitude());
            event.setAltitudeMsl(currentPP.getActualAltitude());
            event.setGroundspeed(current.getGroundspeed());
            event.setHeading(current.getHeading());
            event.setFpAircraft(current.getFpAircraft());
            event.setFpDep(current.getFpDep());
            event.setFpDest(current.getFpDest());
            event.setParsedRegNo(current.getParsedRegNo());

            Connection connx = DB.getConnection();
            try {
                List<TrackingEvent> existingEvents = Persistence.loadWhere(connx,
                        TrackingEvent.class,
                        String.format("pilot_number = %s and dt = '%s'",
                                pilotNumber, Misc.yMdHms.print(dt)));
                if (existingEvents.isEmpty()) {
                    Persistence.create(connx, event);
                    connx.commit();
                    log.info(String.format("    Pilot %s: %s at %s", pilotNumber, event.getEventType(), Misc.yMdHms.print(dt)));
                } else {
                    log.info(String.format("    Pilot %s: %s at %s (event exists)", pilotNumber, event.getEventType(), Misc.yMdHms.print(dt)));
                }
            } finally {
                connx.close();
            }
        }
    }

    private static TrackingEvent.PositionState getPositionState(PilotPosition pp) {
        return pp.isInNearestAirport() ? TrackingEvent.PositionState.InAirport : TrackingEvent.PositionState.Flying;
    }
}
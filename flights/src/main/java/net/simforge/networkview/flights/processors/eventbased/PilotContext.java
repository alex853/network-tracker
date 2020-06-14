package net.simforge.networkview.flights.processors.eventbased;

import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.networkview.core.report.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.processors.eventbased.events.PilotOfflineEvent;
import net.simforge.networkview.flights.processors.eventbased.events.PilotOnlineEvent;
import net.simforge.networkview.flights.processors.eventbased.events.TrackingEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PilotContext {

    private final int pilotNumber;
    protected Flight currFlight;
    protected Position lastProcessedPosition;

    private int positionsWithoutCurrFlight = 0;
    private final List<Flight> recentFlights = new ArrayList<>();
    private final List<TrackingEvent> recentEvents = new ArrayList<>();

//    private boolean dirty = false;

    public PilotContext(int pilotNumber) {
        this.pilotNumber = pilotNumber;
    }

    public PilotContext processPosition(Report report, ReportPilotPosition reportPilotPosition) {
        if (lastProcessedPosition != null
                && (report.getReport().equals(lastProcessedPosition.getReportInfo().getReport())
                || report.getReport().compareTo(lastProcessedPosition.getReportInfo().getReport()) < 0)) {
            throw new IllegalArgumentException("Unable to process the report as it is already processed");
        }

        PilotContext copy = makeCopy();
        copy._processPosition(report, reportPilotPosition);
        return copy;
    }

    private void _processPosition(Report report, ReportPilotPosition reportPilotPosition) {
        Position position;
        if (reportPilotPosition != null) {
            position = Position.create(reportPilotPosition);
        } else {
            position = Position.createOfflinePosition(report);
        }

        boolean wentOnline = position.isPositionKnown() && (lastProcessedPosition == null || !lastProcessedPosition.isPositionKnown());
        boolean wentOffline = !position.isPositionKnown() && (lastProcessedPosition == null || lastProcessedPosition.isPositionKnown());
        if (wentOnline) {
            addEvent(new PilotOnlineEvent(pilotNumber, position.getReportInfo()));
        } else if (wentOffline) {
            addEvent(new PilotOfflineEvent(pilotNumber, position.getReportInfo()));
        }

        this.lastProcessedPosition = position;

        boolean consumed = false;
        if (currFlight != null) {
            consumed = currFlight.offerPosition(position);
        }

        if (!consumed) {
            if (currFlight != null) {
                // so currFlight is finished or terminated
                recentFlights.add(currFlight);
            }

            // if position is known - create new flight with that position
            // if position is unknown - do not create new flight
            currFlight = position.isPositionKnown()
                    ? Flight.start(pilotNumber, position)
                    : null;
        }

        if (currFlight != null) {
            positionsWithoutCurrFlight = 0;
        } else {
            positionsWithoutCurrFlight++;
        }
    }

    public int getPilotNumber() {
        return pilotNumber;
    }

    public Position getLastProcessedPosition() {
        return lastProcessedPosition;
    }

    public Flight getCurrFlight() {
        return currFlight;
    }

    public List<Flight> getRecentFlights() {
        return Collections.unmodifiableList(recentFlights);
    }

    public List<TrackingEvent> getRecentEvents() {
        return Collections.unmodifiableList(recentEvents);
    }

    private void addEvent(TrackingEvent event) {
        recentEvents.add(event);
    }

    public boolean isActive() {
        return currFlight != null || positionsWithoutCurrFlight < 20;
    }

    boolean isDirty() {
        boolean recentFlightsDirty = recentFlights.stream().anyMatch(Flight::isDirty);
        boolean currFlightDirty = currFlight != null && currFlight.isDirty();
        return /*dirty ||*/ currFlightDirty || recentFlightsDirty;
    }

    public PilotContext makeCopy() {
        PilotContext copy = new PilotContext(pilotNumber);

        copy.lastProcessedPosition = lastProcessedPosition;
        copy.currFlight = currFlight != null ? currFlight.makeCopy() : null;

        copy.positionsWithoutCurrFlight = positionsWithoutCurrFlight;
        copy.recentFlights.addAll(recentFlights.stream().map(Flight::makeCopy).collect(Collectors.toList()));

        return copy;
    }

    public void clearRecentFlights() {
        this.recentFlights.clear();
    }
}

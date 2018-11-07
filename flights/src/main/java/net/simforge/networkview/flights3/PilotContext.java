package net.simforge.networkview.flights3;

import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights2.Position;
import net.simforge.networkview.flights3.events.TrackingEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PilotContext {

    public static final int RECENT_FLIGHTS_TIME_LIMIT_HOURS = 36;

    private final int pilotNumber;
    protected Flight currFlight;
    protected Position lastProcessedPosition;

    private int positionsWithoutCurrFlight = 0;
    private final List<Flight> recentFlights = new ArrayList<>();
//    private final List<TrackingEvent> recentEvents = new ArrayList<>();

//    private boolean dirty = false;

    public PilotContext(int pilotNumber) {
        this.pilotNumber = pilotNumber;
    }

    public PilotContext processPosition(Report report, ReportPilotPosition reportPilotPosition) {
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
            // if position is unknow - do not create new flight
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
        throw new UnsupportedOperationException("PilotContext.getRecentEvents");
//        return Collections.unmodifiableList(recentEvents);
    }

//    void addEvent(TrackingEvent event) {
//        recentEvents.add(event);
//    }

    public boolean isActive() {
        return currFlight != null || positionsWithoutCurrFlight < 10;
    }

    public boolean isDirty() {
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
//        copy.recentEvents.addAll(recentEvents);

        return copy;
    }

}

package net.simforge.networkview.flights3;

import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights2.Position;
import net.simforge.networkview.flights3.events.TrackingEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PilotContext {

    private final int pilotNumber;
    private final List<Flight> recentFlights = new ArrayList<>();
    private final List<TrackingEvent> recentEvents = new ArrayList<>();

    private Position currPosition;
    private Flight currFlight;
    private boolean dirty = false;

    public PilotContext(int pilotNumber) {
        this.pilotNumber = pilotNumber;
    }

    public PilotContext processPosition(Report report, ReportPilotPosition reportPilotPosition) {
        // todo remove it!
        recentEvents.clear();

        Position position;
        if (reportPilotPosition != null) {
            position = Position.create(reportPilotPosition);
        } else {
            position = Position.createOfflinePosition(report);
        }
        this.currPosition = position;

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
                    ? Flight.start(this, position)
                    : null;
        }

        return null;
    }

    public int getPilotNumber() {
        return pilotNumber;
    }

    public Position getCurrPosition() {
        return currPosition;
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

    void addEvent(TrackingEvent event) {
        recentEvents.add(event);
    }
}

package net.simforge.networkview.flights2;

import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights2.events.PilotKnownPositionEvent;
import net.simforge.networkview.flights2.events.PilotUnknownPositionEvent;
import net.simforge.networkview.flights2.events.TrackingEvent;
import net.simforge.networkview.flights2.events.TrackingEventHandler;
import net.simforge.networkview.flights2.flight.Flight;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PilotContext {
    private static final int MAX_POSITIONS_IN_MEMORY = 10;

    private final int pilotNumber;
    private final List<Position> positions = new LinkedList<>();
    private final Queue<TrackingEvent> eventsQueue = new LinkedList<>();
    private final ModificationsDelegate delegate = new ModificationsDelegate();

    private String lastProcessedReport;



    private List<Flight> flights; // ordered by first seen date/time, last 3 days flights
    private Flight currentFlight; // if any, can be null
    private List<Object> changes; // changes to persist


    public PilotContext(int pilotNumber) {
        this.pilotNumber = pilotNumber;
    }

    public int getPilotNumber() {
        return pilotNumber;
    }

    public boolean isActive() {
        throw new UnsupportedOperationException("PilotContext.isActive");
    }

    /**
     * It processes pilot position and returns new pilot context.
     * The new context will have updated flights and some changes that have to be persisted.
     */
    public PilotContext processPosition(Report report, ReportPilotPosition reportPilotPosition) {
        if (lastProcessedReport != null
                && report.getReport().compareTo(lastProcessedReport) <= 0) {
            throw new IllegalArgumentException(); // todo message
        }

        PilotContext newContext = this.makeCopy();

        Position position;
        if (reportPilotPosition != null) {
            position = Position.create(reportPilotPosition);
        } else {
            position = Position.createOfflinePosition(report);
        }
        newContext.positions.add(0, position);
        newContext.lastProcessedReport = position.getReport();

        TrackingEvent event;
        if (reportPilotPosition != null) {
            event = new PilotKnownPositionEvent(this);
        } else {
            event = new PilotUnknownPositionEvent(this);
        }

        newContext.delegate.enqueueEvent(event);

        newContext.processEvents();

        return newContext;
    }

    public String getLastProcessedReport() {
        return lastProcessedReport;
    }

    public Position getCurrPosition() {
        if (positions.isEmpty()) {
            return null;
        }
        return positions.get(0);
    }

    public Position getPrevPosition() {
        if (positions.size() < 2) {
            return null;
        }
        return positions.get(1);
    }

    public List<Position> getPositions() {
        throw new UnsupportedOperationException("PilotContext.getPositions");
    }

    public Flight getCurrFlight() {
        throw new UnsupportedOperationException("PilotContext.getCurrFlight");
    }

    private PilotContext makeCopy() {
        PilotContext newContext = new PilotContext(pilotNumber);
        newContext.positions.addAll(positions);
        newContext.eventsQueue.addAll(eventsQueue);
        newContext.lastProcessedReport = lastProcessedReport;
        // todo other fields
        return newContext;
    }

    private void processEvents() {
        while (!eventsQueue.isEmpty()) {
            TrackingEvent event = eventsQueue.poll();
            TrackingEventHandler eventHandler = TrackingEventHandler.registry.get(event.getClass());
            if (eventHandler == null) {
                throw new IllegalStateException(); // todo message
            }

            eventHandler.process(this.delegate, event);

            // todo save event to some other location?
        }

    }

    public List<Object> getChanges() {
        throw new UnsupportedOperationException("PilotContext.getChanges");
    }

    public class ModificationsDelegate {
        public PilotContext getPilotContext() {
            return PilotContext.this;
        }

        public void enqueueEvent(TrackingEvent event) {
            eventsQueue.add(event);
        }
    }
}

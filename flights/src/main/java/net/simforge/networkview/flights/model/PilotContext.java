package flights.model;

import flights.model.events.PilotKnownPositionEvent;
import flights.model.events.PilotUnknownPositionEvent;
import flights.model.events.TrackingEventHandler;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import flights.model.events.TrackingEvent;
import net.simforge.tracker.world.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PilotContext {
    private MainContext mainContext;

    private int pilotNumber;
    private Position position;
    private List<Flight> flights = new ArrayList<>();
    private List<TrackingEvent> events = new ArrayList<>();

    private PilotContext(MainContext mainContext, int pilotNumber, Position position) {
        this.mainContext = mainContext;
        this.pilotNumber = pilotNumber;
        this.position = position;
    }

    public static PilotContext create(MainContext mainContext, int pilotNumber) {
        return new PilotContext(mainContext, pilotNumber, Position.createOfflinePosition());
    }

    public MainContext getMainContext() {
        return mainContext;
    }

    public void processReport(Report report, ReportPilotPosition reportPilotPosition) {
        Position prevPosition = this.position;

        if (reportPilotPosition != null) {
            position = Position.create(reportPilotPosition);
            putEvent(new PilotKnownPositionEvent(this, prevPosition));
        } else {
            position = Position.createOfflinePosition(report);
            putEvent(new PilotUnknownPositionEvent(this, prevPosition));
        }

        processEvents();
    }

    private void processEvents() {
        while (true) {
            TrackingEvent eventToProcess = null;
            for (TrackingEvent event : events) {
                if (!event.isProcessed()) {
                    eventToProcess = event;
                    break;
                }
            }

            if (eventToProcess == null) {
                break;
            }

            TrackingEventHandler eventHandler = TrackingEventHandler.registry.get(eventToProcess.getClass());
            if (eventHandler != null) {
                eventHandler.process(this, eventToProcess);
            }

            eventToProcess.setProcessed(true);
        }
    }

    // todo AK delegate?
    public void putEvent(TrackingEvent event) {
        events.add(event);
    }

    public Position getPosition() {
        return position;
    }

    public int getPilotNumber() {
        return pilotNumber;
    }

    public List<TrackingEvent> getEvents(long reportId) {
        return getEvents(reportId, false);
    }

    public List<TrackingEvent> getEvents(long reportId, boolean includePositionKnownUnknownEvents) {
        List<TrackingEvent> result = new ArrayList<>();
        for (TrackingEvent event : events) {
            if (!includePositionKnownUnknownEvents) {
                if (event instanceof PilotKnownPositionEvent
                        || event instanceof PilotUnknownPositionEvent) {
                    continue;
                }
            }

            if (event.getReportId() == reportId) {
                result.add(event);
            }
        }
        return result;
    }

    public Flight getLastFlight() {
        return flights.isEmpty() ? null : flights.get(flights.size() - 1);
    }

    public Flight getCurrentFlight() {
        Flight lastFlight = getLastFlight();
        if (lastFlight != null) {
            if (lastFlight.getStatus() != FlightStatus.Finished
                && lastFlight.getStatus() != FlightStatus.Terminated) {
                return lastFlight;
            }
        }
        return null;
    }

    public void addFlight(Flight flight) {
        flights.add(flight);
    }

    public List<Flight> getFlights() {
        return Collections.unmodifiableList(flights);
    }
}

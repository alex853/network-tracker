package net.simforge.networkview.flights2;

import net.simforge.networkview.datafeeder.ReportUtils;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights2.events.FlightStatusEvent;
import net.simforge.networkview.flights2.events.FlightplanEvent;
import net.simforge.networkview.flights2.events.PilotKnownPositionEvent;
import net.simforge.networkview.flights2.events.PilotUnknownPositionEvent;
import net.simforge.networkview.flights2.events.TrackingEvent;
import net.simforge.networkview.flights2.events.TrackingEventHandler;
import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.flight.FlightDto;
import net.simforge.networkview.flights2.flight.FlightStatus;
import net.simforge.networkview.flights2.flight.Flightplan;

import java.util.*;
import java.util.stream.Collectors;

public class PilotContext {

    public static final int RECENT_FLIGHTS_TIME_LIMIT_HOURS = 36;

    private final int pilotNumber;
    private final Queue<TrackingEvent> eventsQueue = new LinkedList<>();
    private final List<TrackingEvent> recentEvents = new ArrayList<>();
    private final ModificationsDelegate delegate = new ModificationsDelegate();

    protected Position lastSeenPosition;
    protected Position currPosition;

    // ordered by first seen date/time, last 3 days flights
    // this list does NOT contain currFlight
    protected List<FlightDto> recentFlights = new ArrayList<>();
    protected FlightDto currFlight; // if any, can be null

    private boolean dirty = false;

    public PilotContext(int pilotNumber) {
        this.pilotNumber = pilotNumber;
    }

    public int getPilotNumber() {
        return pilotNumber;
    }

    public boolean isActive(Report report) {
        if (lastSeenPosition == null) {
            return false;
        }

        if (lastSeenPosition.getDt().isBefore(ReportUtils.fromTimestampJava(report.getReport()).minusHours(RECENT_FLIGHTS_TIME_LIMIT_HOURS))) {
            return false;
        }

        return true;
    }

    /**
     * It processes pilot position and returns new pilot context.
     * The new context will have updated flights and some changes that have to be persisted.
     */
    public PilotContext processPosition(Report report, ReportPilotPosition reportPilotPosition) {
        if (currPosition != null) {
            int comparison = report.getReport().compareTo(currPosition.getReport());
            if (comparison < 0) {
                throw new IllegalArgumentException(); // todo message
            } else if (comparison == 0) {
                // todo logger.warn("The report {} is already processed for pilot {}", ....);
                return this;
            }
        }

        PilotContext newContext = this.makeCopy();

        Position position;
        if (reportPilotPosition != null) {
            position = Position.create(reportPilotPosition);
            newContext.lastSeenPosition = position;
            newContext.currPosition = position;
            newContext.dirty = true;
        } else {
            position = Position.createOfflinePosition(report);
            newContext.currPosition = position;
            // we do not mark it as dirty! it allows to skip unnecessary DB updates
        }

        TrackingEvent event;
        if (reportPilotPosition != null) {
            event = new PilotKnownPositionEvent(newContext, this.currPosition);
        } else {
            event = new PilotUnknownPositionEvent(newContext, this.currPosition);
        }

        newContext.delegate.enqueueEvent(event);

        newContext.processEvents();

        return newContext;
    }

    public Position getLastSeenPosition() {
        return lastSeenPosition;
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

    public boolean isDirty() {
        return dirty;
    }

    public PilotContext makeCopy() {
        PilotContext newContext = new PilotContext(pilotNumber);

        newContext.eventsQueue.addAll(eventsQueue);
        newContext.currPosition = currPosition;
        newContext.lastSeenPosition = lastSeenPosition;

        newContext.recentFlights = recentFlights.stream().map(FlightDto::makeCopy).collect(Collectors.toList());
        if (currFlight != null) {
            newContext.currFlight = currFlight.makeCopy();
        }

        return newContext;
    }

    private void processEvents() {
        while (!eventsQueue.isEmpty()) {
            TrackingEvent event = eventsQueue.poll();
            TrackingEventHandler eventHandler = TrackingEventHandler.registry.get(event.getClass());
            if (eventHandler == null) {
                throw new IllegalStateException(); // todo message
            }

            //noinspection unchecked
            eventHandler.process(this.delegate, event);
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
            recentEvents.add(event);
        }

        public void startFlight(Position firstSeenPosition) {
            FlightDto flight = new FlightDto();

            flight.setStatus(firstSeenPosition.isOnGround() ? FlightStatus.Departure : FlightStatus.Flying);
            flight.setFirstSeen(firstSeenPosition);
            flight.setLastSeen(firstSeenPosition);

            // todo processCriteria(flight, firstSeenPosition);

            collectFlightplan(flight);

            putMovementStatusEvent(flight);

            // we do not add it to recentFlights list
            currFlight = flight;

            dirty = true;
        }

        public void continueFlight(Flight _flight) {
            FlightDto flight = (FlightDto) _flight;

            Position position = getPilotContext().getCurrPosition();

            flight.setLastSeen(position);

            // todo processCriteria(flight, position);

            collectFlightplan(flight);

            dirty = true;
        }

        public void finishFlight(Flight _flight) {
            FlightDto flight = (FlightDto) _flight;

            flight.setStatus(FlightStatus.Finished);

            putMovementStatusEvent(flight);

            recentFlights.add(currFlight);
            currFlight = null;

            dirty = true;
        }

        public void terminateFlight(Flight _flight) {
            FlightDto flight = (FlightDto) _flight;

            flight.setStatus(FlightStatus.Terminated);

            putMovementStatusEvent(flight);

            recentFlights.add(currFlight);
            currFlight = null;

            dirty = true;
        }

        public void lostFlight(Flight _flight) {
            FlightDto flight = (FlightDto) _flight;

            flight.setStatus(FlightStatus.Lost);

            putMovementStatusEvent(flight);

            dirty = true;
        }

        public void resumeLostFlight(Flight _flight) {
            FlightDto flight = (FlightDto) _flight;

            Position position = getPilotContext().getCurrPosition();

            flight.setStatus(FlightStatus.Flying);
            flight.setLastSeen(position);

            // todo processCriteria(flight, position);

            collectFlightplan(flight);

            putMovementStatusEvent(flight);

            dirty = true;
        }

        public void takeoff(Flight _flight, Position onGroundPositionBeforeTakeoff) {
            FlightDto flight = (FlightDto) _flight;

            Position position = getPilotContext().getCurrPosition();

            flight.setStatus(FlightStatus.Flying);
            flight.setDeparture(onGroundPositionBeforeTakeoff);
            flight.setLastSeen(position);

            // todo processCriteria(flight, position);

            collectFlightplan(flight);

            putMovementStatusEvent(flight);

            dirty = true;
        }

        public void landing(Flight _flight) {
            FlightDto flight = (FlightDto) _flight;

            Position position = getPilotContext().getCurrPosition();

            flight.setStatus(FlightStatus.Arrival);
            flight.setDestination(position);
            flight.setLastSeen(position);

            // todo processCriteria(flight, position);

            collectFlightplan(flight);

            putMovementStatusEvent(flight);

            dirty = true;
        }

        private void collectFlightplan(FlightDto flight) {
            Position position = getPilotContext().getCurrPosition();
            Flightplan flightplan = Flightplan.fromPosition(position);
            if (flightplan != null) {
                if (!flightplan.equals(flight.getFlightplan())) {
                    flight.setFlightplan(flightplan);

                    enqueueEvent(new FlightplanEvent(getPilotContext(), flight));
                }
            }
        }

        private void putMovementStatusEvent(Flight flight) {
            enqueueEvent(new FlightStatusEvent(getPilotContext(), flight));
        }
    }
}

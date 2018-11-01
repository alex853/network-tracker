package net.simforge.networkview.flights2;

import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.model.Flightplan;
import net.simforge.networkview.flights2.events.FlightStatusEvent;
import net.simforge.networkview.flights2.events.FlightplanEvent;
import net.simforge.networkview.flights2.events.PilotKnownPositionEvent;
import net.simforge.networkview.flights2.events.PilotUnknownPositionEvent;
import net.simforge.networkview.flights2.events.TrackingEvent;
import net.simforge.networkview.flights2.events.TrackingEventHandler;
import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.flight.FlightDto;
import net.simforge.networkview.flights2.flight.FlightStatus;

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




    private List<Object> changes; // changes to persist


    public PilotContext(int pilotNumber) {
        this.pilotNumber = pilotNumber;
    }

    public int getPilotNumber() {
        return pilotNumber;
    }

    public boolean isActive() {
        return true; // todo think about it!
    }

    /**
     * It processes pilot position and returns new pilot context.
     * The new context will have updated flights and some changes that have to be persisted.
     */
    public PilotContext processPosition(Report report, ReportPilotPosition reportPilotPosition) {
        if (currPosition != null
                && report.getReport().compareTo(currPosition.getReport()) <= 0) {
            throw new IllegalArgumentException(); // todo message
        }

        PilotContext newContext = this.makeCopy();

        Position position;
        if (reportPilotPosition != null) {
            position = Position.create(reportPilotPosition);
            newContext.lastSeenPosition = position;
        } else {
            position = Position.createOfflinePosition(report);
        }
        newContext.currPosition = position;

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

    public PilotContext makeCopy() {
        PilotContext newContext = new PilotContext(pilotNumber);

        newContext.eventsQueue.addAll(eventsQueue);
        newContext.currPosition = currPosition;

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
            flight.setOrigin(firstSeenPosition);
            flight.setLastSeen(firstSeenPosition);

            // todo processCriteria(flight, firstSeenPosition);

            collectFlightplan(flight);

            putMovementStatusEvent(flight);

            // we do not add it to recentFlights list
            currFlight = flight;
        }

        public void continueFlight(Flight _flight) {
            FlightDto flight = (FlightDto) _flight;

            Position position = getPilotContext().getCurrPosition();

            flight.setLastSeen(position);

            if (flight.getStatus().is(FlightStatus.Departure)
                    && position.isPositionKnown()
                    && position.isOnGround()) {
                flight.setOrigin(position);
            }

            // todo processCriteria(flight, position);

            collectFlightplan(flight);
        }

        public void finishFlight(Flight _flight) {
            FlightDto flight = (FlightDto) _flight;

            flight.setStatus(FlightStatus.Finished);

            putMovementStatusEvent(flight);

            recentFlights.add(currFlight);
            currFlight = null;
        }

        public void terminateFlight(Flight _flight) {
            FlightDto flight = (FlightDto) _flight;

            flight.setStatus(FlightStatus.Terminated);

            putMovementStatusEvent(flight);

            recentFlights.add(currFlight);
            currFlight = null;
        }

        public void lostFlight(Flight _flight) {
            FlightDto flight = (FlightDto) _flight;

            flight.setStatus(FlightStatus.Lost);

            putMovementStatusEvent(flight);
        }

        public void takeoff(Flight _flight) {
            FlightDto flight = (FlightDto) _flight;

            Position position = getPilotContext().getCurrPosition();

            flight.setStatus(FlightStatus.Flying);
            flight.setLastSeen(position);

            // todo processCriteria(flight, position);

            collectFlightplan(flight);

            putMovementStatusEvent(flight);
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
        }

        private void collectFlightplan(FlightDto flight) {
            Position position = getPilotContext().getCurrPosition();
            Flightplan flightplan = flightplanFromPosition(position);
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

    // todo move it to Flightplan
    private static Flightplan flightplanFromPosition(Position position) {
        if (position.hasFlightplan()) {
            return new Flightplan(position.getFpAircraftType(), position.getFpOrigin(), position.getFpDestination());
        } else {
            return null;
        }
    }
}

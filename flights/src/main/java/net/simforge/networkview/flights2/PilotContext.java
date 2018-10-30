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
import net.simforge.networkview.flights2.flight.FlightStatus;

import java.util.*;
import java.util.stream.Collectors;

public class PilotContext {
    private static final int MAX_POSITIONS_IN_MEMORY = 10;

    private final int pilotNumber;
    private final List<Position> positions = new LinkedList<>();
    private final Queue<TrackingEvent> eventsQueue = new LinkedList<>();
    private final List<TrackingEvent> recentEvents = new ArrayList<>();
    private final ModificationsDelegate delegate = new ModificationsDelegate();

    private String lastProcessedReport;

    private List<FlightImpl> flights = new ArrayList<>(); // ordered by first seen date/time, last 3 days flights
    private FlightImpl currFlight; // if any, can be null




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
        return currFlight;
    }

    public List<TrackingEvent> getRecentEvents() {
        return Collections.unmodifiableList(recentEvents);
    }

    private PilotContext makeCopy() {
        PilotContext newContext = new PilotContext(pilotNumber);

        newContext.positions.addAll(positions);
        newContext.eventsQueue.addAll(eventsQueue);
        newContext.lastProcessedReport = lastProcessedReport;

        newContext.flights = flights.stream().map(FlightImpl::makeCopy).collect(Collectors.toList());
        if (currFlight != null) {
            newContext.currFlight = newContext.flights.get(newContext.flights.size() - 1);
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
            FlightImpl flight = new FlightImpl();

            flight.setStatus(firstSeenPosition.isOnGround() ? FlightStatus.Departure : FlightStatus.Flying);
            flight.setFirstSeen(firstSeenPosition);
            flight.setOrigin(firstSeenPosition);
            flight.setLastSeen(firstSeenPosition);

            // todo processCriteria(flight, firstSeenPosition);

            collectFlightplan(flight);

            putMovementStatusEvent(flight);

            flights.add(flight);
            currFlight = flight;
        }

        public void continueFlight(Flight _flight) {
            FlightImpl flight = (FlightImpl) _flight;

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

        public void finishFlight(Flight flight) {
            throw new UnsupportedOperationException("ModificationsDelegate.terminateFlight");
        }

        public void lostFlight(Flight _flight) {
            FlightImpl flight = (FlightImpl) _flight;

            flight.setStatus(FlightStatus.Lost);

            putMovementStatusEvent(flight);
        }

        public void terminateFlight(Flight flight) {
            throw new UnsupportedOperationException("ModificationsDelegate.terminateFlight");
        }

        public void takeoff(Flight _flight) {
            FlightImpl flight = (FlightImpl) _flight;

            Position position = getPilotContext().getCurrPosition();

            flight.setStatus(FlightStatus.Flying);
            flight.setLastSeen(position);

            // todo processCriteria(flight, position);

            collectFlightplan(flight);

            putMovementStatusEvent(flight);
        }

        public void landing(Flight _flight) {
            FlightImpl flight = (FlightImpl) _flight;

            Position position = getPilotContext().getCurrPosition();

            flight.setStatus(FlightStatus.Arrival);
            flight.setDestination(position);
            flight.setLastSeen(position);

            // todo processCriteria(flight, position);

            collectFlightplan(flight);

            putMovementStatusEvent(flight);
        }

        private void collectFlightplan(FlightImpl flight) {
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

    private class FlightImpl implements Flight {
        private FlightStatus status;

        private Position firstSeen;
        private Position origin;
        private Position destination;
        private Position lastSeen;

        private Flightplan flightplan;

        @Override
        public FlightStatus getStatus() {
            return status;
        }

        public void setStatus(FlightStatus status) {
            this.status = status;
        }

        @Override
        public Position getFirstSeen() {
            return firstSeen;
        }

        public void setFirstSeen(Position firstSeen) {
            this.firstSeen = firstSeen;
        }

        @Override
        public Position getOrigin() {
            return origin;
        }

        public void setOrigin(Position origin) {
            this.origin = origin;
        }

        @Override
        public Position getDestination() {
            return destination;
        }

        public void setDestination(Position destination) {
            this.destination = destination;
        }

        @Override
        public Position getLastSeen() {
            return lastSeen;
        }

        public void setLastSeen(Position lastSeen) {
            this.lastSeen = lastSeen;
        }

        @Override
        public Flightplan getFlightplan() {
            return flightplan;
        }

        public void setFlightplan(Flightplan flightplan) {
            this.flightplan = flightplan;
        }

        public FlightImpl makeCopy() {
            FlightImpl copy = new FlightImpl();
            copy.status = status;
            copy.firstSeen = firstSeen;
            copy.origin = origin;
            copy.destination = destination;
            copy.lastSeen = lastSeen;
            copy.flightplan = flightplan;
            return copy;
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

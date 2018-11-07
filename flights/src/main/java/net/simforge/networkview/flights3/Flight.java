package net.simforge.networkview.flights3;

import net.simforge.networkview.flights2.Position;
import net.simforge.networkview.flights2.events.*;
import net.simforge.networkview.flights2.flight.FlightStatus;
import net.simforge.networkview.flights2.flight.Flightplan;
import net.simforge.networkview.flights3.criteria.EllipseCriterion;
import net.simforge.networkview.flights3.criteria.OnGroundJumpCriterion;
import net.simforge.networkview.flights3.criteria.TrackTrailCriterion;
import net.simforge.networkview.flights3.events.FlightStatusEvent;
import net.simforge.networkview.flights3.events.FlightplanEvent;
import net.simforge.networkview.flights3.events.PilotLandingEvent;
import net.simforge.networkview.flights3.events.PilotTakeoffEvent;
import net.simforge.networkview.flights3.events.TrackingEvent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Flight {
    private final int pilotNumber;
    private FlightStatus status;
    private String callsign;
    private Position firstSeen;
    private Position lastSeen;
    private Position takeoff;
    private Position landing;
    private Flightplan flightplan;

    private LinkedList<Position> track = new LinkedList<>();

    private List<TrackingEvent> recentEvents = new LinkedList<>();
    private boolean dirty;

    private Flight(int pilotNumber) {
        this.pilotNumber = pilotNumber;
    }

    public boolean offerPosition(Position position) {
        Position prevPosition = track.getLast();

        boolean wentOnline = !prevPosition.isPositionKnown() && position.isPositionKnown();
        boolean wentOffline = prevPosition.isPositionKnown() && !position.isPositionKnown();
        boolean isOnline = prevPosition.isPositionKnown() && position.isPositionKnown();

        boolean takeoff = isOnline && prevPosition.isOnGround() && !position.isOnGround();
        boolean landing = isOnline && !prevPosition.isOnGround() && position.isOnGround();

        boolean aircraftTypeEnduranceExceeded = false; // todo
        boolean trackTrailCorrupted = true; // todo

        switch (status) {
            case Departure:
            case Preparing:
            case Departing:
                if (wentOffline) {
                    terminateFlight(position);
                    return false;
                }

                if (OnGroundJumpCriterion.get(this).meets(position)
                        || !TrackTrailCriterion.meetsOrInapplicable(this, position)) {
                    finishOrTerminateFlight(position);
                    return false;
                }

                boolean moving = false; // todo
                if (takeoff) {
                    takeoffFlight(position, prevPosition);
                    collectFlightplan();
                    return true;
                } else if (moving && (status == FlightStatus.Departure || status == FlightStatus.Preparing)) {
                    setStatus(FlightStatus.Departing, position.getReport());
                }

                continueFlight(position);
                collectFlightplan();

                return true;

            case Flying:
                if (wentOffline) {
                    lostFlight(position);
                    return true;
                }

                if (!TrackTrailCriterion.meetsOrInapplicable(this, position)
                        && !EllipseCriterion.get(this).meets(position)) {
                    terminateFlight(position);
                    return false;
                }

                if (landing) {
                    landFlight(position);
                    collectFlightplan();
                    return true;
                }

                continueFlight(position);
                collectFlightplan();

                return true;

            case Arrival:
            case Arriving:
            case Arrived:
                boolean callsignChanged = false; // todo
                boolean flightplanChanged = false; // todo
                boolean tooMuchTimeSinceLanding = false; // todo
                if (wentOffline
                        || OnGroundJumpCriterion.get(this).meets(position)
                        || callsignChanged
                        || flightplanChanged
                        || tooMuchTimeSinceLanding) {
                    finishFlight(position);
                    return false;
                }

                boolean stoppedForSomeTime = false; // todo
                if (stoppedForSomeTime && (status == FlightStatus.Arrival || status == FlightStatus.Arriving)) {
                    setStatus(FlightStatus.Arrived, position.getReport());
                    continueFlight(position);
                }

                return true;

            case Lost:
                if (aircraftTypeEnduranceExceeded) {
                    finishOrTerminateFlight(position);
                    return false;
                }

                if (wentOnline) {
                    if (TrackTrailCriterion.meetsOrInapplicable(this, position)
                            && EllipseCriterion.get(this).meets(position)) {
                        resumeLostFlight(position);
                        collectFlightplan();
                        return true;
                    } else {
                        terminateFlight(position);
                        return false;
                    }
                }

                return true;

            default:
                throw new IllegalStateException();
        }
    }

    public static Flight load(int pilotNumber, FlightStatus status, String callsign,
                              Position firstSeen, Position lastSeen,
                              Position takeoff, Position landing,
                              Flightplan flightplan,
                              List<Position> track) {
        Flight flight = new Flight(pilotNumber);
        flight.status = status;
        flight.callsign = callsign;
        flight.firstSeen = firstSeen;
        flight.lastSeen = lastSeen;
        flight.takeoff = takeoff;
        flight.landing = landing;
        flight.flightplan = flightplan;
        flight.track.addAll(track);
        return flight;
    }

    static Flight start(int pilotNumber, Position firstSeen) {
        Flight flight = new Flight(pilotNumber);

        flight.callsign = firstSeen.getCallsign();

        flight.firstSeen = firstSeen;
        flight.lastSeen = firstSeen;

        if (firstSeen.isOnGround()) {
            flight.setStatus(FlightStatus.Departure, firstSeen.getReport()); // todo which status?
        } else {
            flight.setStatus(FlightStatus.Flying, firstSeen.getReport());
        }

        flight.track.add(firstSeen);

        flight.collectFlightplan();

        flight.dirty = true;

        return flight;
    }

    private void continueFlight(Position position) {
        lastSeen = position;

        track.add(position);

        dirty = true;
    }

    private void finishOrTerminateFlight(Position position) {
        if (status.is(FlightStatus.Arrival)) {
            finishFlight(position);
        } else if (status.is(FlightStatus.Flying) || status.is(FlightStatus.Departure)) {
            terminateFlight(position);
        } else {
            throw new IllegalStateException(); // todo message
        }
    }

    private void terminateFlight(Position position) {
        setStatus(FlightStatus.Terminated, position.getReport());

        dirty = true;
    }

    private void finishFlight(Position position) {
        setStatus(FlightStatus.Finished, position.getReport());

        dirty = true;
    }

    private void lostFlight(Position position) {
        setStatus(FlightStatus.Lost, position.getReport());

        track.add(position);

        dirty = true;
    }

    private void resumeLostFlight(Position position) {
        setStatus(FlightStatus.Flying, position.getReport());

        lastSeen = position;

        track.add(position);

        dirty = true;
    }

    private void takeoffFlight(Position position, Position prevPosition) {
        addEvent(new PilotTakeoffEvent(pilotNumber, position.getReport()));

        setStatus(FlightStatus.Flying, position.getReport());

        takeoff = prevPosition;
        lastSeen = position;

        track.add(position);

        dirty = true;
    }

    private void landFlight(Position position) {
        addEvent(new PilotLandingEvent(pilotNumber, position.getReport()));

        setStatus(FlightStatus.Arrival, position.getReport());

        landing = position;
        lastSeen = position;

        track.add(position);

        dirty = true;
    }

    private void collectFlightplan() {
        Position position = track.getLast();
        Flightplan flightplan = Flightplan.fromPosition(position);
        if (flightplan != null) {
            if (!flightplan.equals(this.flightplan)) {
                this.flightplan = flightplan;
                addEvent(new FlightplanEvent(pilotNumber, position.getReport()));
            }
        }
    }

    private void setStatus(FlightStatus status, String report) {
        this.status = status;
        addEvent(new FlightStatusEvent(pilotNumber, report, this.getStatus()));
    }

    private void addEvent(TrackingEvent event) {
        recentEvents.add(event);
    }

    public FlightStatus getStatus() {
        return status;
    }

    public String getCallsign() {
        return callsign;
    }

    public Position getFirstSeen() {
        return firstSeen;
    }

    public Position getLastSeen() {
        return lastSeen;
    }

    public Position getTakeoff() {
        return takeoff;
    }

    public Position getLanding() {
        return landing;
    }

    public Flightplan getFlightplan() {
        return flightplan;
    }

    // todo hide it from public interface
    public LinkedList<Position> getTrack() {
        return track;
    }

    public boolean isDirty() {
        return dirty;
    }

    public List<TrackingEvent> getRecentEvents() {
        return Collections.unmodifiableList(recentEvents);
    }

    public Flight makeCopy() {
        Flight copy = new Flight(pilotNumber);
        copy.status = status;
        copy.callsign = callsign;
        copy.firstSeen = firstSeen;
        copy.lastSeen = lastSeen;
        copy.takeoff = takeoff;
        copy.landing = landing;
        copy.flightplan = flightplan;
        copy.track.addAll(track);
        copy.dirty = false;
        return copy;
    }

}

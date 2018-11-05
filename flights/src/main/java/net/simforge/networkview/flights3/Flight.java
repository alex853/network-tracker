package net.simforge.networkview.flights3;

import net.simforge.networkview.flights2.Position;
import net.simforge.networkview.flights2.flight.FlightStatus;
import net.simforge.networkview.flights2.flight.Flightplan;
import net.simforge.networkview.flights3.criteria.EllipseCriterion;
import net.simforge.networkview.flights3.criteria.OnGroundJumpCriterion;
import net.simforge.networkview.flights3.events.FlightStatusEvent;
import net.simforge.networkview.flights3.events.FlightplanEvent;
import net.simforge.networkview.flights3.events.PilotLandingEvent;
import net.simforge.networkview.flights3.events.PilotTakeoffEvent;

import java.util.LinkedList;

public class Flight {
    private FlightStatus status;
    private String callsign;
    private Position firstSeen;
    private Position lastSeen;
    private Position takeoff;
    private Position landing;
    private Flightplan flightplan;

    private LinkedList<Position> track = new LinkedList<>();

    private PilotContext pilotContext;
    private boolean dirty;

    private Flight() {
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
//        boolean ellipseOK = EllipseCriterion.get(this).meets(position);

        switch (status) {
            case Departure:
            case Preparing:
            case Departing:
                if (OnGroundJumpCriterion.get(this).meets(position)) {
                    finishOrTerminateFlight();
                    return false;
                }

                boolean moving = false; // todo
                if (takeoff) {
                    takeoffFlight(position, prevPosition);
                    collectFlightplan();
                    return true;
                } else if (moving && (status == FlightStatus.Departure || status == FlightStatus.Preparing)) {
                    setStatus(FlightStatus.Departing);
                }

                continueFlight(position);
                collectFlightplan();

                return true;

            case Flying:
                if (wentOffline) {
                    lostFlight(position);
                    return true;
                }

                if (/*trackTrailCorrupted &&*/ !EllipseCriterion.get(this).meets(position)) {
                    terminateFlight();
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
                    finishFlight();
                    return false;
                }

                boolean stoppedForSomeTime = false; // todo
                if (stoppedForSomeTime && (status == FlightStatus.Arrival || status == FlightStatus.Arriving)) {
                    setStatus(FlightStatus.Arrived);
                    continueFlight(position);
                }

                return true;

            case Lost:
                if (aircraftTypeEnduranceExceeded) {
                    finishOrTerminateFlight();
                    return false;
                }

                if (wentOnline) {
                    if (/*!trackTrailCorrupted ||*/ EllipseCriterion.get(this).meets(position)) {
                        resumeLostFlight(position);
                        collectFlightplan();
                        return true;
                    } else {
                        terminateFlight();
                        return false;
                    }
                }

                return true;

            default:
                throw new IllegalStateException();
        }
    }

    static Flight start(PilotContext pilotContext, Position firstSeen) {
        Flight flight = new Flight();

        flight.pilotContext = pilotContext;

        flight.callsign = firstSeen.getCallsign();

        flight.firstSeen = firstSeen;
        flight.lastSeen = firstSeen;

        if (firstSeen.isOnGround()) {
            flight.setStatus(FlightStatus.Departure); // todo which status?
        } else {
            flight.setStatus(FlightStatus.Flying);
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

    private void finishOrTerminateFlight() {
        if (status.is(FlightStatus.Arrival)) {
            finishFlight();
        } else if (status.is(FlightStatus.Flying) || status.is(FlightStatus.Departure)) {
            terminateFlight();
        } else {
            throw new IllegalStateException(); // todo message
        }
    }

    private void terminateFlight() {
        setStatus(FlightStatus.Terminated);

        dirty = true;
    }

    private void finishFlight() {
        setStatus(FlightStatus.Finished);

        dirty = true;
    }

    private void lostFlight(Position position) {
        setStatus(FlightStatus.Lost);

        track.add(position);

        dirty = true;
    }

    private void resumeLostFlight(Position position) {
        setStatus(FlightStatus.Flying);

        lastSeen = position;

        track.add(position);

        dirty = true;
    }

    private void takeoffFlight(Position position, Position prevPosition) {
        pilotContext.addEvent(new PilotTakeoffEvent(pilotContext.getPilotNumber(), position.getReport()));

        setStatus(FlightStatus.Flying);

        takeoff = prevPosition;
        lastSeen = position;

        track.add(position);

        dirty = true;
    }

    private void landFlight(Position position) {
        pilotContext.addEvent(new PilotLandingEvent(pilotContext.getPilotNumber(), position.getReport()));

        setStatus(FlightStatus.Arrival);

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
                pilotContext.addEvent(new FlightplanEvent(pilotContext));
            }
        }
    }

    private void setStatus(FlightStatus status) {
        this.status = status;
        pilotContext.addEvent(new FlightStatusEvent(pilotContext, this));
    }

    public FlightStatus getStatus() {
        return status;
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
}

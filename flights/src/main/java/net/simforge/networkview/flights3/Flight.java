package net.simforge.networkview.flights3;

import net.simforge.commons.misc.Geo;
import net.simforge.commons.misc.JavaTime;
import net.simforge.networkview.flights2.Position;
import net.simforge.networkview.flights2.flight.FlightStatus;
import net.simforge.networkview.flights2.flight.Flightplan;
import net.simforge.networkview.flights3.criteria.EllipseCriterion;
import net.simforge.networkview.flights3.criteria.OnGroundJumpCriterion;
import net.simforge.networkview.flights3.criteria.TrackTrailCriterion;
import net.simforge.networkview.flights3.events.*;

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
    private Double distanceFlown;
    private Double flightTime;

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
                    increaseDistanceAndTime(position, prevPosition);
                    return true;
                } else if (moving && (status == FlightStatus.Departure || status == FlightStatus.Preparing)) {
                    setStatus(FlightStatus.Departing, position.getReportInfo().getReport());
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
                    increaseDistanceAndTime(position, prevPosition);
                    return true;
                }

                continueFlight(position);
                collectFlightplan();
                increaseDistanceAndTime(position, prevPosition);

                return true;

            case Arrival:
            case Arriving:
            case Arrived:
                boolean callsignChanged = false; // todo
                boolean flightplanChanged = false; // todo
                boolean tooMuchTimeSinceLanding = JavaTime.hoursBetween(this.landing.getReportInfo().getDt(), position.getReportInfo().getDt()) > 0.5; // todo test for this condition
                if (wentOffline
                        || OnGroundJumpCriterion.get(this).meets(position)
                        || callsignChanged
                        || flightplanChanged
                        || tooMuchTimeSinceLanding) {
                    finishFlight(position);
                    return false;
                }

                boolean stoppedForSomeTime = false; // todo test for this condition
                if (stoppedForSomeTime && (status == FlightStatus.Arrival || status == FlightStatus.Arriving)) {
                    setStatus(FlightStatus.Arrived, position.getReportInfo().getReport());
                    continueFlight(position);
                    return true;
                }

                continueFlight(position); // todo test for this line
                return true;

            case Lost:
                // todo the condition below should be replaced by normal endurance condition
                if (JavaTime.hoursBetween(lastSeen.getReportInfo().getDt(), position.getReportInfo().getDt()) > 6.0) {
                    terminateFlight(position);
                    return false;
                }

                if (aircraftTypeEnduranceExceeded) { // todo more complicated condition
                    terminateFlight(position);
                    return false;
                }

                if (wentOnline) {
                    if (TrackTrailCriterion.meetsOrInapplicable(this, position)
                            && EllipseCriterion.get(this).meets(position)) {
                        Position prevSeenPosition = lastSeen;
                        resumeLostFlight(position);
                        collectFlightplan();
                        increaseDistanceAndTime(position, prevSeenPosition);
                        return true;
                    } else {
                        terminateFlight(position);
                        return false;
                    }
                }

                this.track.add(position); // just eat this position
                return true;

            default:
                throw new IllegalStateException();
        }
    }

    private void increaseDistanceAndTime(Position position, Position prevPosition) {
        distanceFlown = (distanceFlown != null ? distanceFlown : 0) + Geo.distance(prevPosition.getCoords(), position.getCoords());
        flightTime = (flightTime != null ? flightTime : 0) + JavaTime.hoursBetween(prevPosition.getReportInfo().getDt(), position.getReportInfo().getDt());
    }

    public static Flight load(int pilotNumber, FlightStatus status, String callsign,
                              Position firstSeen, Position lastSeen,
                              Position takeoff, Position landing,
                              Flightplan flightplan,
                              Double distanceFlown,
                              Double flightTime,
                              List<Position> track) {
        Flight flight = new Flight(pilotNumber);
        flight.status = status;
        flight.callsign = callsign;
        flight.firstSeen = firstSeen;
        flight.lastSeen = lastSeen;
        flight.takeoff = takeoff;
        flight.landing = landing;
        flight.flightplan = flightplan;
        flight.distanceFlown = distanceFlown;
        flight.flightTime = flightTime;
        flight.track.addAll(track);
        return flight;
    }

    static Flight start(int pilotNumber, Position firstSeen) {
        Flight flight = new Flight(pilotNumber);

        flight.callsign = firstSeen.getCallsign();

        flight.firstSeen = firstSeen;
        flight.lastSeen = firstSeen;

        if (firstSeen.isOnGround()) {
            flight.setStatus(FlightStatus.Departure, firstSeen.getReportInfo().getReport()); // todo which status?
        } else {
            flight.setStatus(FlightStatus.Flying, firstSeen.getReportInfo().getReport());
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
        setStatus(FlightStatus.Terminated, position.getReportInfo().getReport());

        dirty = true;
    }

    private void finishFlight(Position position) {
        setStatus(FlightStatus.Finished, position.getReportInfo().getReport());

        dirty = true;
    }

    private void lostFlight(Position position) {
        setStatus(FlightStatus.Lost, position.getReportInfo().getReport());

        track.add(position);

        dirty = true;
    }

    private void resumeLostFlight(Position position) {
        setStatus(FlightStatus.Flying, position.getReportInfo().getReport());

        lastSeen = position;

        track.add(position);

        dirty = true;
    }

    private void takeoffFlight(Position position, Position prevPosition) {
        addEvent(new PilotTakeoffEvent(pilotNumber, position.getReportInfo().getReport()));

        setStatus(FlightStatus.Flying, position.getReportInfo().getReport());

        takeoff = prevPosition;
        lastSeen = position;

        track.add(position);

        dirty = true;
    }

    private void landFlight(Position position) {
        addEvent(new PilotLandingEvent(pilotNumber, position.getReportInfo().getReport()));

        setStatus(FlightStatus.Arrival, position.getReportInfo().getReport());

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
                addEvent(new FlightplanEvent(pilotNumber, position.getReportInfo().getReport()));
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

    public Double getDistanceFlown() {
        return distanceFlown;
    }

    public Double getFlightTime() {
        return flightTime;
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
        copy.distanceFlown = distanceFlown;
        copy.flightTime = flightTime;
        copy.track.addAll(track);
        copy.dirty = false;
        return copy;
    }

}

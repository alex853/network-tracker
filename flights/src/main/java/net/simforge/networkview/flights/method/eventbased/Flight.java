package net.simforge.networkview.flights.method.eventbased;

import net.simforge.commons.misc.Geo;
import net.simforge.commons.misc.JavaTime;
import net.simforge.networkview.core.report.ReportInfo;
import net.simforge.networkview.flights.method.eventbased.criteria.OnGroundJumpCriterion;
import net.simforge.networkview.flights.method.eventbased.criteria.TrackTrailCriterion;
import net.simforge.networkview.flights.method.eventbased.events.*;
import net.simforge.networkview.flights.EllipseCriterion;

import java.util.Collections;
import java.util.Iterator;
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

    boolean offerPosition(Position position) {
        Position prevPosition = track.getLast();

        boolean wentOnline = !prevPosition.isPositionKnown() && position.isPositionKnown();
        boolean wentOffline = prevPosition.isPositionKnown() && !position.isPositionKnown();
        boolean isOnline = prevPosition.isPositionKnown() && position.isPositionKnown();

        boolean takeoff = isOnline && prevPosition.isOnGround() && !position.isOnGround();
        boolean landing = isOnline && !prevPosition.isOnGround() && position.isOnGround();

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
                    // todo add event 'jump found!!!'
                    finishOrTerminateFlight(position);
                    return false;
                }

                boolean moving = position.getGroundspeed() >= Consts.MOVING_GROUNDSPEED_LIMIT_KTS;
                if (takeoff) {
                    takeoffFlight(position, prevPosition);
                    collectFlightplan();
                    increaseDistanceAndTime(position, prevPosition);
                    return true;
                } else if (moving && (status == FlightStatus.Departure || status == FlightStatus.Preparing)) {
                    setStatus(FlightStatus.Departing, position.getReportInfo());
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
                        && !meetsEllipseCriterion(position)) {
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
            case TouchedDown:
            case Arriving:
            case Arrived:
                boolean callsignChanged = position.isPositionKnown() && prevPosition.isPositionKnown()
                        && !position.getCallsign().equals(prevPosition.getCallsign());
                // todo boolean flightplanChanged = false;
                boolean tooMuchTimeSinceLanding = JavaTime.hoursBetween(this.landing.getReportInfo().getDt(), position.getReportInfo().getDt()) > Consts.FROM_LANDING_TO_ARRIVED_MAX_TIME_HRS;
                if (wentOffline
                        || OnGroundJumpCriterion.get(this).meets(position)
                        || callsignChanged
                        // todo || flightplanChanged
                        || tooMuchTimeSinceLanding) {
                    finishFlight(position);
                    return false;
                }

                if (status == FlightStatus.TouchedDown) {
                    if (takeoff) {
                        touchAndGoFlight(position, prevPosition);
                        return true;
                    } else {
                        setStatus(FlightStatus.Arriving, position.getReportInfo());
                    }
                }

                boolean stoppedRightNow = position.getGroundspeed() == 0;
                double distanceInLastMinutes = calcDistanceInLastMinutes(position, Consts.TIME_TO_PARK_AFTER_ARRIVAL_MINUTES);
                boolean parked = stoppedRightNow && distanceInLastMinutes <= Consts.DISTANCE_TO_PARK_AFTER_ARRIVAL_NM;
                if (parked && (status == FlightStatus.Arrival || status == FlightStatus.Arriving)) {
                    setStatus(FlightStatus.Arrived, position.getReportInfo());
                    continueFlight(position);
                    return true;
                }

                continueFlight(position);
                return true;

            case Lost:
                boolean aircraftTypeEnduranceExceeded = false; // todo

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
                            && meetsEllipseCriterion(position)) {
                        // todo add event 'flight resumed because of ellipse or track trail criterion'
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

    private boolean meetsEllipseCriterion(Position position) {
        return new EllipseCriterion(takeoff, flightplan).meets(position);
    }

    private double calcDistanceInLastMinutes(Position position, int minutes) {
        double result = 0;

        Position prev = position;
        Iterator<Position> it = track.descendingIterator();
        while (it.hasNext()) {
            Position curr = it.next();

            if (JavaTime.hoursBetween(curr.getReportInfo().getDt(), position.getReportInfo().getDt()) > minutes / 60.0) {
                break;
            }

            if (!curr.isPositionKnown()) {
                continue;
            }

            result += Geo.distance(prev.getCoords(), curr.getCoords());

            prev = curr;
        }

        return result;
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
            boolean moving = firstSeen.getGroundspeed() >= Consts.MOVING_GROUNDSPEED_LIMIT_KTS;
            if (!moving) {
                flight.setStatus(FlightStatus.Preparing, firstSeen.getReportInfo());
            } else {
                flight.setStatus(FlightStatus.Departing, firstSeen.getReportInfo());
            }
        } else {
            flight.setStatus(FlightStatus.Flying, firstSeen.getReportInfo());
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
            throw new IllegalStateException("Can't finish/terminate flight in inappropriate status, flight status is " + status.toString());
        }
    }

    private void terminateFlight(Position position) {
        setStatus(FlightStatus.Terminated, position.getReportInfo());

        dirty = true;
    }

    private void finishFlight(Position position) {
        setStatus(FlightStatus.Finished, position.getReportInfo());

        dirty = true;
    }

    private void lostFlight(Position position) {
        setStatus(FlightStatus.Lost, position.getReportInfo());

        track.add(position);

        dirty = true;
    }

    private void resumeLostFlight(Position position) {
        setStatus(FlightStatus.Flying, position.getReportInfo());

        lastSeen = position;

        track.add(position);

        dirty = true;
    }

    private void takeoffFlight(Position position, Position prevPosition) {
        addEvent(new PilotTakeoffEvent(pilotNumber, position.getReportInfo()));

        setStatus(FlightStatus.Flying, position.getReportInfo());

        takeoff = prevPosition;
        lastSeen = position;

        track.add(position);

        dirty = true;
    }

    private void landFlight(Position position) {
        addEvent(new PilotLandingEvent(pilotNumber, position.getReportInfo()));

        setStatus(FlightStatus.TouchedDown, position.getReportInfo());

        landing = position;
        lastSeen = position;

        track.add(position);

        dirty = true;
    }

    private void touchAndGoFlight(Position position, Position prevPosition) {
        addEvent(new PilotTouchAndGoEvent(pilotNumber, position.getReportInfo()));

        setStatus(FlightStatus.Flying, position.getReportInfo());

        landing = null;
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
                addEvent(new FlightplanEvent(pilotNumber, position.getReportInfo()));
            }
        }
    }

    private void setStatus(FlightStatus status, ReportInfo reportInfo) {
        this.status = status;
        addEvent(new FlightStatusEvent(pilotNumber, reportInfo, this.getStatus()));
    }

    private void addEvent(TrackingEvent event) {
        recentEvents.add(event);
    }

    public int getPilotNumber() {
        return pilotNumber;
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

    @SuppressWarnings("WeakerAccess")
    public List<TrackingEvent> getRecentEvents() {
        return Collections.unmodifiableList(recentEvents);
    }

    @SuppressWarnings("WeakerAccess")
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

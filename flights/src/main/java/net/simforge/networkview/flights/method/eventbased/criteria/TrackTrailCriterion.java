package net.simforge.networkview.flights.method.eventbased.criteria;

import net.simforge.commons.misc.Geo;
import net.simforge.commons.misc.JavaTime;
import net.simforge.networkview.flights.Criterion;
import net.simforge.networkview.flights.method.eventbased.Flight;
import net.simforge.networkview.core.Position;

import java.util.Iterator;
import java.util.LinkedList;

public class TrackTrailCriterion implements Criterion {
    private Flight flight;

    private static final double minimalExpectedAreaRadiusNm = 25;
    private static final double expectedAreaSectorAngleDegrees = 20;
    private static final int maxTrackLength = 10;

    private int trackSegmentsConsidered;
    private Position trackLastSeen;
    private double trackLength;
    private double trackTime;
    private double trackSpeed;
    private double trackBearing;

    private TrackTrailCriterion(Flight flight) {
        this.flight = flight;
        checkTrackData();
    }

    public boolean applicable(Position position) {
        if (!position.isPositionKnown()) {
            return false;
        }

        return trackSegmentsConsidered >= 10;
    }

    @Override
    public boolean meets(Position position) {
        if (!applicable(position)) {
            return false;
        }

        Position lastSeenPosition = trackLastSeen;
        double timeSinceLastSeen = JavaTime.hoursBetween(lastSeenPosition.getReportInfo().getDt(), position.getReportInfo().getDt());
        double distanceFromLastSeenPosition = trackSpeed * timeSinceLastSeen;

        Geo.Coords expectedAreaCenter = Geo.destination(lastSeenPosition.getCoords(), trackBearing, distanceFromLastSeenPosition);
        double expectedAreaRadius = minimalExpectedAreaRadiusNm + Math.sin(Math.toRadians(expectedAreaSectorAngleDegrees)) * distanceFromLastSeenPosition;

        double actualDistanceToExpectedAreaCenter = Geo.distance(expectedAreaCenter, position.getCoords());

        return actualDistanceToExpectedAreaCenter <= expectedAreaRadius;
    }

    private void checkTrackData() {
        trackSegmentsConsidered = 0;
        trackLastSeen = null;
        trackLength = Double.NaN;
        trackTime = Double.NaN;
        trackSpeed = Double.NaN;
        trackBearing = Double.NaN;


        LinkedList<Position> track = flight.getTrack();
        Iterator<Position> positionIterator = track.descendingIterator();

        Position p1 = null;
        Position p2 = null;

        // seek lastSeen position in the iterator
        while (positionIterator.hasNext()) {
            Position p = positionIterator.next();
            if (p.isPositionKnown()) {
                p2 = p;
                trackLastSeen = p;
                break;
            }
        }

        if (p2 == null) {
            throw new IllegalStateException("Unable to find LastSeen position in flight track");
        }

        double bearingSum = 0;
        trackLength = 0;
        trackTime = 0;
        while (positionIterator.hasNext()) {
            Position p = positionIterator.next();
            if (!p.isPositionKnown()) {
                break;
            }

            trackSegmentsConsidered++;
            p1 = p;

            trackLength += Geo.distance(p1.getCoords(), p2.getCoords());
            trackTime += JavaTime.hoursBetween(p1.getReportInfo().getDt(), p2.getReportInfo().getDt());
            bearingSum += Geo.bearing(p1.getCoords(), p2.getCoords());

            if (trackSegmentsConsidered >= maxTrackLength) {
                break;
            }

            if (p1.isOnGround()) {
                break;
            }

            p2 = p1;
        }

        if (trackSegmentsConsidered > 0) {
            trackSpeed = trackTime != 0 ? trackLength / trackTime : Double.NaN;
            trackBearing = bearingSum / trackSegmentsConsidered;
        }
    }

    @Override
    public String toString() {
        return "{S=" + trackSegmentsConsidered +
                ", L=" + (!Double.isNaN(trackLength) ? (int) trackLength : "?") +
                ", T=" + (!Double.isNaN(trackTime) ? (int) (trackTime * 60) : "?") +
                ", Spd=" + (!Double.isNaN(trackSpeed) ? (int) trackSpeed : "?") +
                ", Brg=" + (!Double.isNaN(trackBearing) ? (int) trackBearing : "?") +
                "}" ;
    }

/*    public TrackTrailCriterion makeCopy() {
        TrackTrailCriterion copy = new TrackTrailCriterion(flight);
        copy.positions.addAll(positions);
        copy.trackDataCalculated = trackDataCalculated;
        copy.trackLength = trackLength;
        copy.trackTime = trackTime;
        copy.trackSpeed = trackSpeed;
        copy.trackBearing = trackBearing;
        return copy;
    }*/

    public static Criterion get(Flight flight) {
        return new TrackTrailCriterion(flight);
    }

    public static boolean meetsOrInapplicable(Flight flight, Position position) {
        TrackTrailCriterion criterion = (TrackTrailCriterion) get(flight);
        boolean applicable = criterion.applicable(position);
        if (!applicable) {
            return true;
        }
        return criterion.meets(position);
    }

}

package flights.model.criteria;

import net.simforge.commons.misc.Geo;
import flights.model.Flight;
import net.simforge.tracker.world.Position;

import java.util.LinkedList;

public class TrackTrailCriterion implements Criterion {
    private Flight flight;

    private double minimalExpectedAreaRadiusNm = 50;
    private double expectedAreaSectorAngleDegrees = 15;
    private int maxTrackLength = 10;

    private LinkedList<Position> positions = new LinkedList<Position>();

    private boolean trackDataCalculated = false;
    private double trackLength;
    private double trackTime;
    private double trackSpeed;
    private double trackBearing;

    public TrackTrailCriterion(Flight flight) {
        this.flight = flight;
    }

    @Override
    public boolean meets(Position position) {
        if (!hasEnoughPositions()) {
            return false;
        }

        if (!position.isPositionKnown()) {
            return false;
        }

        checkTrackData();

        Position lastSeenPosition = positions.getLast();
        double timeSinceLastSeen = flight.getPilotContext().getMainContext().getTimeBetween(lastSeenPosition.getReportId(), position.getReportId());
        double distanceFromLastSeenPosition = trackSpeed * timeSinceLastSeen;

        Geo.Coords expectedAreaCenter = Geo.destination(lastSeenPosition.getCoords(), trackBearing, distanceFromLastSeenPosition);
        double expectedAreaRadius = minimalExpectedAreaRadiusNm + Math.sin(Math.toRadians(expectedAreaSectorAngleDegrees)) * distanceFromLastSeenPosition;

        double actualDistanceToExpectedAreaCenter = Geo.distance(expectedAreaCenter, position.getCoords());

        return actualDistanceToExpectedAreaCenter <= expectedAreaRadius;
    }

    private boolean hasEnoughPositions() {
        return positions.size() >= 2;
    }

    @Override
    public void process(Position position) {
        if (!position.isPositionKnown() || position.isOnGround()) {
            positions.clear();
            return;
        }

        positions.add(position);
        trackDataCalculated = false;

        while (positions.size() > maxTrackLength) {
            positions.remove(0);
            trackDataCalculated = false;
        }
    }

    private void checkTrackData() {
        if (trackDataCalculated) {
            return;
        }

        trackLength = 0;
        trackTime = 0;
        double bearingSum = 0;

        for (int i = 0; i < positions.size() - 1; i++) {
            Position p1 = positions.get(i);
            Position p2 = positions.get(i + 1);

            trackLength += Geo.distance(p1.getCoords(), p2.getCoords());
            trackTime += flight.getPilotContext().getMainContext().getTimeBetween(p1.getReportId(), p2.getReportId());
            bearingSum += Geo.bearing(p1.getCoords(), p2.getCoords());
        }

        trackSpeed = trackTime != 0 ? trackLength / trackTime : Double.NaN;

        if (positions.size() >= 2) {
            trackBearing = bearingSum / (positions.size() - 1);
        } else {
            trackBearing = Double.NaN;
        }

        trackDataCalculated = true;
    }

    @Override
    public String toString() {
        checkTrackData();
        if (positions.isEmpty()) {
            return "{No P}";
        } else {
            return "{P=" + positions.size() +
                    ", R=" + (positions.isEmpty() ? "-" : positions.get(positions.size() - 1).getReportId()) +
                    ", L=" + (int) trackLength +
                    ", T=" + (int) (trackTime * 60) +
                    ", Spd=" + (!Double.isNaN(trackSpeed) ? (int) trackSpeed : "-") +
                    ", Brg=" + (!Double.isNaN(trackBearing) ? (int) trackBearing : "-") +
                    "}" ;
        }
    }
}

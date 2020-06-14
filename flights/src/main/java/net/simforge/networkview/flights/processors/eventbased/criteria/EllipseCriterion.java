package net.simforge.networkview.flights.processors.eventbased.criteria;

import net.simforge.commons.misc.Geo;
import net.simforge.networkview.flights.processors.eventbased.Flight;
import net.simforge.networkview.flights.processors.eventbased.Flightplan;
import net.simforge.networkview.flights.processors.eventbased.Position;
import net.simforge.networkview.world.airports.Airport;
import net.simforge.networkview.world.airports.Airports;

public class EllipseCriterion implements Criterion {
    private Flight flight;

    private EllipseCriterion(Flight flight) {
        this.flight = flight;
    }

    @Override
    public boolean meets(Position position) {
        Position takeoffPosition = flight.getTakeoff();
        if (takeoffPosition == null) {
            return false;
        }
        if (!takeoffPosition.isInAirport()) {
            return false;
        }

        Flightplan flightplan = flight.getFlightplan();
        if (flightplan == null) {
            return false;
        }
        String destinationIcao = flightplan.getDestination();
        if (destinationIcao == null) {
            return false;
        }
        Airport destinationAirport = Airports.get().getByIcao(destinationIcao);
        if (destinationAirport == null) {
            return false;
        }

        Geo.Coords takeoffCoords = takeoffPosition.getCoords();
        Geo.Coords destinationCoords = destinationAirport.getCoords();
        Geo.Coords positionCoords = position.getCoords();

        double dist = Geo.distance(takeoffCoords, destinationCoords);

        // https://ru.wikipedia.org/wiki/%D0%AD%D0%BB%D0%BB%D0%B8%D0%BF%D1%81
        double c = dist / 2;
        double a = c + 100; // 100 nm
        double b = Math.sqrt( a*a - c*c );

        double summedDist = Geo.distance(takeoffCoords, positionCoords) + Geo.distance(positionCoords, destinationCoords);

        return summedDist <= 2 * a;
    }

    public static Criterion get(Flight flight) {
        return new EllipseCriterion(flight);
    }
}

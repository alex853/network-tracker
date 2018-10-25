package flights.model.criteria;

import flights.model.Flight;
import net.simforge.commons.misc.Geo;
import flights.model.Flightplan;
import net.simforge.tracker.world.Position;
import net.simforge.tracker.world.airports.Airport;
import net.simforge.tracker.world.airports.Airports;

public class EllipseCriterion implements Criterion {
    private Flight flight;

    private EllipseCriterion(Flight flight) {
        this.flight = flight;
    }

    @Override
    public void process(Position position) {
        // no/op
    }

    @Override
    public boolean meets(Position position) {
        Position originPosition = flight.getOrigin();
        if (!originPosition.isInAirport()) {
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

        Geo.Coords originCoords = originPosition.getCoords();
        Geo.Coords destinationCoords = destinationAirport.getCoords();
        Geo.Coords positionCoords = position.getCoords();

        double dist = Geo.distance(originCoords, destinationCoords);

        // https://ru.wikipedia.org/wiki/%D0%AD%D0%BB%D0%BB%D0%B8%D0%BF%D1%81
        double c = dist / 2;
        double a = c + 100; // 100 nm
        double b = Math.sqrt( a*a - c*c );

        double summedDist = Geo.distance(originCoords, positionCoords) + Geo.distance(positionCoords, destinationCoords);

        return summedDist <= 2 * a;
    }

    public static Criterion get(Flight flight) {
        return new EllipseCriterion(flight);
    }
}

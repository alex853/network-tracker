package net.simforge.networkview.flights2.flight;

import net.simforge.networkview.flights.model.Flightplan;
import net.simforge.networkview.flights2.Position;

public interface Flight {

    FlightStatus getStatus();

    Position getFirstSeen();

    Position getOrigin();

    Position getDestination();

    Position getLastSeen();

    Flightplan getFlightplan();

}

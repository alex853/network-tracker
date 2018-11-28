package net.simforge.networkview.flights2.flight;

import net.simforge.networkview.flights2.Position;

@Deprecated
public interface Flight {

    FlightStatus getStatus();

    Position getFirstSeen();

    Position getDeparture();

    Position getDestination();

    Position getLastSeen();

    Flightplan getFlightplan();

}

package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights2.PilotContext;
import net.simforge.networkview.flights2.Position;
import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.flight.FlightStatus;

@Deprecated
public class PilotTakeoffEvent extends PilotEvent {
    private Position onGroundPositionBeforeTakeoff;

    public PilotTakeoffEvent(int pilotNumber, String report, Position onGroundPositionBeforeTakeoff) {
        super(pilotNumber, report, "pilot/takeoff");
        this.onGroundPositionBeforeTakeoff = onGroundPositionBeforeTakeoff;
    }

    static {
        TrackingEventHandler.registry.put(PilotTakeoffEvent.class, (TrackingEventHandler<PilotTakeoffEvent>) (delegate, event) -> {
            PilotContext pilotContext = delegate.getPilotContext();
            Flight flight = pilotContext.getCurrFlight();

            if (flight.getStatus().is(FlightStatus.Arrival)) {
                delegate.finishFlight(flight);
                delegate.startFlight(flight.getLastSeen());
                flight = pilotContext.getCurrFlight();
            }

            if (!flight.getStatus().is(FlightStatus.Departure)) {
                throw new IllegalStateException("Takeoff is not suitable for flight in '" + flight.getStatus() + "' status");
            }

            delegate.takeoff(flight, event.onGroundPositionBeforeTakeoff);
        });
    }
}

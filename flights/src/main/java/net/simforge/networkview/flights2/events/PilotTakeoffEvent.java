package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.PilotContext;
import net.simforge.networkview.flights2.flight.FlightStatus;

public class PilotTakeoffEvent extends PilotEvent {
    public PilotTakeoffEvent(int pilotNumber, String report) {
        super(pilotNumber, report, "pilot/takeoff");
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

            delegate.takeoff(flight);
        });
    }
}

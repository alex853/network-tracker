package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights2.PilotContext;
import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.flight.FlightStatus;

@Deprecated
public class PilotLandingEvent extends PilotEvent {
    public PilotLandingEvent(int pilotNumber, String report) {
        super(pilotNumber, report, "pilot/landing");
    }

    static {
        TrackingEventHandler.registry.put(PilotLandingEvent.class, (TrackingEventHandler<PilotLandingEvent>) (delegate, event) -> {
            PilotContext pilotContext = delegate.getPilotContext();
            Flight flight = pilotContext.getCurrFlight();

            if (!flight.getStatus().is(FlightStatus.Flying)) {
                throw new IllegalStateException("Landing is not suitable for flight in '" + flight.getStatus() + "' status");
            }

            delegate.landing(flight);
        });
    }
}

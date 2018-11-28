package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights2.PilotContext;
import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.flight.FlightStatus;

@Deprecated
public class PilotOfflineEvent extends PilotEvent {
    public PilotOfflineEvent(int pilotNumber, String report) {
        super(pilotNumber, report, "pilot/offline");
    }

    static {
        TrackingEventHandler.registry.put(PilotOfflineEvent.class, (TrackingEventHandler<PilotOfflineEvent>) (delegate, event) -> {
            PilotContext pilotContext = delegate.getPilotContext();
            Flight flight = pilotContext.getCurrFlight();

            if (flight != null) {
                if (flight.getStatus() == FlightStatus.Arrival) {
                    delegate.finishFlight(flight);
                } else if (flight.getStatus() == FlightStatus.Flying) {
                    delegate.lostFlight(flight);
                } else {
                    delegate.terminateFlight(flight);
                }
            }
        });
    }
}

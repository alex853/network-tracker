package net.simforge.networkview.flights2;

public interface PersistenceLayer {
    PilotContext createContext(int pilotNumber);

    PilotContext loadContext(int pilotNumber);

    PilotContext saveChanges(PilotContext pilotContext);
}

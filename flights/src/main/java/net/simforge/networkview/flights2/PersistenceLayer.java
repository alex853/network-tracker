package net.simforge.networkview.flights2;

import java.io.IOException;

public interface PersistenceLayer {
    PilotContext createContext(int pilotNumber);

    PilotContext loadContext(int pilotNumber) throws IOException;

    PilotContext saveChanges(PilotContext pilotContext);
}

package net.simforge.networkview.flights2;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface PersistenceLayer {
    List<PilotContext> loadActivePilotContexts(LocalDateTime lastProcessedReportDt) throws IOException; // todo remove exception

    PilotContext createContext(int pilotNumber);

    PilotContext loadContext(int pilotNumber) throws IOException; // todo remove exception

    PilotContext saveChanges(PilotContext pilotContext);
}

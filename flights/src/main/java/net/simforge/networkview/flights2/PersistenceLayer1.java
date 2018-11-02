package net.simforge.networkview.flights2;

import java.time.LocalDateTime;
import java.util.List;

public class PersistenceLayer1 implements PersistenceLayer {
    public List<PilotContext> loadActivePilotContexts(LocalDateTime lastProcessedReportDt) {
        throw new UnsupportedOperationException("PersistenceLayer1.loadActivePilotContexts");
    }

    public PilotContext loadContext(int pilotNumber) {
        throw new UnsupportedOperationException("PersistenceLayer1.loadContext");
    }

    public PilotContext createContext(int pilotNumber) {
        throw new UnsupportedOperationException("PersistenceLayer1.createContext");
    }

    public PilotContext saveChanges(PilotContext pilotContext) {
        throw new UnsupportedOperationException("PersistenceLayer1.saveChanges");
    }
}

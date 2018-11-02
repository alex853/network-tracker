package net.simforge.networkview.flights2;

import net.simforge.networkview.datafeeder.persistence.Report;

import java.time.LocalDateTime;
import java.util.List;

public class NoOpPersistenceLayer implements PersistenceLayer {
    @Override
    public List<PilotContext> loadActivePilotContexts(LocalDateTime lastProcessedReportDt) {
        throw new UnsupportedOperationException("NoOpPersistenceLayer.loadActivePilotContexts");
    }

    @Override
    public PilotContext createContext(int pilotNumber, Report seenReport) {
        return new PilotContext(pilotNumber);
    }

    @Override
    public PilotContext loadContext(int pilotNumber) {
        return null; // we do not save flights and we do not load contexts because of that
    }

    @Override
    public PilotContext saveChanges(PilotContext pilotContext) {
        return pilotContext; // we do not save anything
    }
}

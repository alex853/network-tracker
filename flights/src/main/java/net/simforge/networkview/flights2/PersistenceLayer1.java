package net.simforge.networkview.flights2;

public class PersistenceLayer1 implements PersistenceLayer {
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

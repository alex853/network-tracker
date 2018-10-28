package net.simforge.networkview.flights2;

public class InMemoryNoOpPersistenceLayer implements PersistenceLayer {
    @Override
    public PilotContext createContext(int pilotNumber) {
        return new PilotContext(pilotNumber);
    }

    @Override
    public PilotContext loadContext(int pilotNumber) {
        return null; // we do not save flights and we do not load contexts because of that
    }

    @Override
    public PilotContext saveChanges(PilotContext pilotContext) {
        throw new UnsupportedOperationException("StupidInMemoryPersistenceLayer.saveChanges");
    }
}

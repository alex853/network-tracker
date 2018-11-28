package net.simforge.networkview.flights2.events;

@Deprecated
public class BaseEvent implements TrackingEvent {
    private int pilotNumber;
    private String report;
    private String type;

    protected BaseEvent(int pilotNumber, String report, String type) {
        this.pilotNumber = pilotNumber;
        this.report = report;
        this.type = type;
    }

    @Override
    public int getPilotNumber() {
        return pilotNumber;
    }

    @Override
    public String getReport() {
        return report;
    }

    @Override
    public String getType() {
        return type;
    }
}

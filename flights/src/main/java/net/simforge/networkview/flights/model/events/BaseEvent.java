package flights.model.events;

public class BaseEvent implements TrackingEvent {
    private int pilotNumber;
    private long reportId;
    private String type;
    private boolean processed;

    protected BaseEvent(int pilotNumber, long reportId, String type) {
        this.pilotNumber = pilotNumber;
        this.reportId = reportId;
        this.type = type;
    }

    @Override
    public int getPilotNumber() {
        return pilotNumber;
    }

    @Override
    public long getReportId() {
        return reportId;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isProcessed() {
        return processed;
    }

    @Override
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}

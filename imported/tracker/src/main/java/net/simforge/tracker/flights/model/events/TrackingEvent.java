package net.simforge.tracker.flights.model.events;

public interface TrackingEvent {

    int getPilotNumber();

    long getReportId();

    String getType();

    boolean isProcessed();

    void setProcessed(boolean processed);

}

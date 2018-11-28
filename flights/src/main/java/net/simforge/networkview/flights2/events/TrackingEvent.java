package net.simforge.networkview.flights2.events;

@Deprecated
public interface TrackingEvent {

    int getPilotNumber();

    String getReport();

    String getType();

}

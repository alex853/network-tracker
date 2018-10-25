package net.simforge.tracker.flights.model.events;

import net.simforge.tracker.flights.model.PilotContext;

import java.util.HashMap;
import java.util.Map;

public interface TrackingEventHandler {

    Map<Class, TrackingEventHandler> registry = new HashMap<>();

    void process(PilotContext pilotContext, TrackingEvent event);

}

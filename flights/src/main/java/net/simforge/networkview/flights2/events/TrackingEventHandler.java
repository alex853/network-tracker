package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights2.PilotContext;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public interface TrackingEventHandler<TrackingEventClass> {

    Map<Class, TrackingEventHandler> registry = new HashMap<>();

    void process(PilotContext.ModificationsDelegate delegate, TrackingEventClass event);

}

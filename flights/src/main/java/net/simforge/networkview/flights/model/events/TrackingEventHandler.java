package flights.model.events;

import flights.model.PilotContext;

import java.util.HashMap;
import java.util.Map;

public interface TrackingEventHandler {

    Map<Class, TrackingEventHandler> registry = new HashMap<>();

    void process(PilotContext pilotContext, TrackingEvent event);

}

package net.simforge.networkview.flights.method.eventbased.criteria;

import net.simforge.networkview.flights.method.eventbased.Position;

public interface Criterion {
    boolean meets(Position position);
}

package net.simforge.networkview.flights.processors.eventbased.criteria;

import net.simforge.networkview.flights.processors.eventbased.Position;

public interface Criterion {
    boolean meets(Position position);
}

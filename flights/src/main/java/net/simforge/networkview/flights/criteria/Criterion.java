package net.simforge.networkview.flights.criteria;

import net.simforge.networkview.flights.Position;

public interface Criterion {
    boolean meets(Position position);
}

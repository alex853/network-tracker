package net.simforge.tracker.flights.model.criteria;

import net.simforge.tracker.world.Position;

public interface Criterion {
    void process(Position position);

    boolean meets(Position position);
}

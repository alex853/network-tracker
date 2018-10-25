package net.simforge.networkview.flights.model.criteria;

import net.simforge.networkview.datafeeder.Position;

public interface Criterion {
    void process(Position position);

    boolean meets(Position position);
}

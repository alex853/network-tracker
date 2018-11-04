package net.simforge.networkview.flights2.criteria;

import net.simforge.networkview.flights2.Position;

public interface Criterion {
    void process(Position position);

    boolean meets(Position position);
}

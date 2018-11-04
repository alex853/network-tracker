package net.simforge.networkview.flights2.criteria;

import net.simforge.networkview.flights2.Position;

public interface Criterion {
    boolean meets(Position position);
}

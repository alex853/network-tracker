package net.simforge.networkview.flights2.criteria;

import net.simforge.networkview.flights2.Position;

@Deprecated
public interface Criterion {
    boolean meets(Position position);
}

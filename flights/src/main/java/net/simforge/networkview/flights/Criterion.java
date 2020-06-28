package net.simforge.networkview.flights;

import net.simforge.networkview.core.Position;

public interface Criterion {
    boolean meets(Position position);
}

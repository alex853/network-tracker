package net.simforge.networkview.flights;

public enum FlightStatus {
    Departure(100, null),
      Preparing(110, Departure),
      Departing(150, Departure),
    Flying(200, null),
      Lost(299, Flying),
    Arrival(300, null),
      Arriving(310, Arrival),
      Arrived(370, Arrival),
    Finished(400, null),
    Terminated(499, null);

    private int code;
    private FlightStatus parent;

    private FlightStatus(int code, FlightStatus parent) {
        this.code = code;
        this.parent = parent;
    }

    public boolean is(FlightStatus other) {
        if (other == null) {
            return false;
        }

        for (FlightStatus t = this; t != null; t = t.parent) {
            if (other == t) {
                return true;
            }
        }

        return false;
    }

    public static FlightStatus byCode(int code) {
        for (FlightStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        String s = "";

        for (FlightStatus t = this; t != null; t = t.parent) {
            s = t.name() + (s.length() != 0 ? "." : "") + s;
        }

        return s;
    }

    public boolean in(FlightStatus... statuses) {
        for (FlightStatus status : statuses) {
            if (this.is(status)) {
                return true;
            }
        }
        return false;
    }
}

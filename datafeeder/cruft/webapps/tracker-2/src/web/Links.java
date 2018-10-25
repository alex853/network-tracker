package web;

import forge.Aircraft;
import forge.commons.Misc;
import entities.Movement;

public class Links {
    public static String airport(String icao) {
        return airport(icao, "");
    }

    public static String airport(String icao, String ifNull) {
        if (icao != null) {
            return "<a href=\"airport.jsp?icao=" + icao + "\">" + icao + "</a>";
        } else {
            return ifNull;
        }
    }

    public static String airportClazz(String icao, String clazz) {
        return airportClazz(icao, clazz, "");
    }

    public static String airportClazz(String icao, String clazz, String ifNull) {
        if (icao != null) {
            return "<a href=\"airport.jsp?icao=" + icao + "\"" + (clazz != null ? " class=\"" + clazz + "\"" : "") +">" + icao + "</a>";
        } else {
            return ifNull;
        }
    }
    public static String pilot(int pilotNumber) {
        return "<a href=\"pilot.jsp?pilot=" + pilotNumber + "\">" + pilotNumber + "</a>";
    }

    public static String aircraft(Aircraft aircraft) {
        return "<a href=\"aircraft.jsp?aircraftId=" + aircraft.getId() + "\">" + aircraft.getType() + "</a>";
    }

    public static String movement(Movement movement) {
        return "<a href=\"flight.jsp?flightId=" + movement.getId() + "\">" + Misc.valueOf(movement.getCallsign(), "n/a") + "</a>";
    }
}

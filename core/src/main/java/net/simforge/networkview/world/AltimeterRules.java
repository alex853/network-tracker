package net.simforge.networkview.world;

import net.simforge.atmosphere.ActualAltitude;
import net.simforge.atmosphere.AltimeterMode;
import net.simforge.commons.misc.Geo;
import net.simforge.refdata.airports.Airport;
import net.simforge.refdata.airports.Airports;

public class AltimeterRules {
    private boolean valid;
    private AltimeterMode altimeterPolicy;
    private int transitionAltitude;
    private int transitionLevel;
    private double qnhMb;

    private AltimeterRules() {
    }

    public static AltimeterRules get(Geo.Coords location, double qnhMb) {
        AltimeterRules rules = new AltimeterRules();

        Airport nearestAirport = Airports.get().findNearest(location);

        if (nearestAirport == null) {
            rules.valid = false;
            return rules;
        }

        rules.valid = true;
        rules.qnhMb = qnhMb;

        rules.altimeterPolicy = AltimeterMode.QNH;
        rules.transitionAltitude = 5000;

        if (nearestAirport.getIcao().startsWith("U")) {
            rules.altimeterPolicy = AltimeterMode.QFE;
            rules.transitionAltitude = 3000;
        } else if (nearestAirport.getIcao().startsWith("K") || nearestAirport.getIcao().startsWith("P")) {
            rules.transitionAltitude = 18000;
        } else if (nearestAirport.getIcao().startsWith("EG")) {
            rules.transitionAltitude = 6000;
        }

        // calculate TL
        int minimalLayerThickness = 1000;
        int tlAltitude = rules.transitionAltitude + minimalLayerThickness;
        ActualAltitude altQnh = ActualAltitude.get(tlAltitude, qnhMb);
        rules.transitionLevel = (int) (Math.ceil(altQnh.getActualAltitude() / 1000.0) * 1000);

        return rules;
    }

    public boolean isValid() {
        return valid;
    }

    public AltimeterMode getAltimeterPolicy() {
        checkValid();

        return altimeterPolicy;
    }

    public int getTransitionAltitude() {
        checkValid();

        return transitionAltitude;
    }

    public int getTransitionLevel() {
        checkValid();

        return transitionLevel;
    }

    public int getActualAltitude(int altitude) {
        checkValid();

        AltimeterMode altimeterMode;
        if (altitude < transitionLevel) {
            altimeterMode = altimeterPolicy;
        } else {
            altimeterMode = AltimeterMode.STD;
        }

        // let's calculate actual altitude
        int actualAltitude;
        if (altimeterMode == AltimeterMode.QNH) {
            actualAltitude = altitude;
        } else if (altimeterMode == AltimeterMode.QFE) {
            actualAltitude = altitude;
        } else { // STD
            ActualAltitude altQnh = ActualAltitude.get(altitude, qnhMb);
            actualAltitude = altQnh.getActualAltitude();
        }

        return actualAltitude;
    }

    private void checkValid() {
        if (!valid) {
            throw new IllegalArgumentException("AltimeterRules object is in invalid state");
        }
    }

    public String formatAltitude(int altitude) {
        checkValid();

        AltimeterMode altimeterMode;
        if (altitude < transitionLevel) {
            altimeterMode = altimeterPolicy;
        } else {
            altimeterMode = AltimeterMode.STD;
        }

        return ActualAltitude.formatAltitude(altitude, altimeterMode);
    }
}

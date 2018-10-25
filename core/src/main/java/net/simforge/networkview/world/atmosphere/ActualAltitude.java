package net.simforge.networkview.world.atmosphere;

import net.simforge.commons.misc.Str;

public class ActualAltitude {
    private int reportedAltitude;
    private double reportedQnhMb;

    private int actualAltitude;
    private int actualFlightLevel;

    private int nearestX1000FlightLevel;

    public static ActualAltitude get(int reportedAltitude, double reportedQnhMb) {
        return new ActualAltitude(reportedAltitude, reportedQnhMb);
    }

    private ActualAltitude(int reportedAltitude, double reportedQnhMb) {
        this.reportedAltitude = reportedAltitude;
        this.reportedQnhMb = reportedQnhMb;

        doCalculations();
    }

    private void doCalculations() {
        double pstd = Atmosphere.QNH_STD_PRECISE;
        double altpress =  (1 - Math.pow((reportedQnhMb / pstd), 0.190284)) * 145366.45;
        int pressureCorrection = (int) Math.round(altpress);

        actualAltitude = reportedAltitude + pressureCorrection;
        if (actualAltitude < 0)
            actualAltitude = 0;

        actualFlightLevel = (int) Math.round(actualAltitude / 100.0) * 100;

        nearestX1000FlightLevel = (int) Math.round(actualAltitude / 1000.0) * 1000;
    }

    public int getReportedAltitude() {
        return reportedAltitude;
    }

    public double getReportedQnhMb() {
        return reportedQnhMb;
    }

    public int getActualAltitude() {
        return actualAltitude;
    }

    public int getActualFlightLevel() {
        return actualFlightLevel;
    }

    public int getNearestX1000FlightLevel() {
        return nearestX1000FlightLevel;
    }

    public boolean isOnX1000FlightLevel() {
        int deviation = actualAltitude - nearestX1000FlightLevel;
        return Math.abs(deviation) < 200;
    }

    @Override
    public String toString() {
        return String.format("Reported %s -> Actual %s on QNH %s", reportedAltitude, actualAltitude, reportedQnhMb);
    }

    public static String formatAltitude(int altitude, AltimeterMode altimeterMode) {
        String prefix;

        switch (altimeterMode) {
            case STD:
                prefix = "FL";
                break;
            case QNH:
            case QFE:
                prefix = "A";
                break;
            default:
                throw new IllegalArgumentException("Don't know how to format altitude for altimeter mode " + altimeterMode);
        }

        return prefix + Str.z((int) Math.round(altitude / 100.0), 3);
    }
}

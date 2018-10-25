package web;

public class FLCalc {
    private int reportedAltitude;
    private int reportedQnhMb;
    private int pressureCorrection;
    private int altitude;
    private int actualFlightLevel;
    private int nearestX1000FlightLevel;
    private int deviation;
    private boolean onX1000FlightLevel;

    public static FLCalc get(int reportedAltitude, int reportedQnhMb) {
        return new FLCalc(reportedAltitude, reportedQnhMb);
    }

    private FLCalc(int reportedAltitude, int reportedQnhMb) {
        this.reportedAltitude = reportedAltitude;
        this.reportedQnhMb = reportedQnhMb;
        calc();
    }

    private void calc() {
        double pstd = 1013.25;
        double altpress =  (1 - Math.pow((reportedQnhMb/pstd), 0.190284)) * 145366.45;
        pressureCorrection = (int) Math.round(altpress);

        altitude = reportedAltitude + pressureCorrection;
        if (altitude < 0)
            altitude = 0;
        
        actualFlightLevel = altitude / 100;
        nearestX1000FlightLevel = (int) Math.round(altitude / 1000.0) * 10;
        deviation = altitude - nearestX1000FlightLevel * 100;
        onX1000FlightLevel = Math.abs(deviation) < 200;
    }

    public int getReportedAltitude() {
        return reportedAltitude;
    }

    public int getReportedQnhMb() {
        return reportedQnhMb;
    }

    public int getPressureCorrection() {
        return pressureCorrection;
    }

    public int getAltitude() {
        return altitude;
    }

    public int getActualFlightLevel() {
        return actualFlightLevel;
    }

    public int getNearestX1000FlightLevel() {
        return nearestX1000FlightLevel;
    }

    public int getDeviation() {
        return deviation;
    }

    public boolean isOnX1000FlightLevel() {
        return onX1000FlightLevel;
    }

    public static String printFL(int fl) {
        String r = String.valueOf(fl);
        while (r.length() < 3) {
            r = "0" + r;
        }
        return r;
    }

    public static String altToFL(int alt) {
        return printFL( (int) Math.round(alt / 100.0) );
    }
}

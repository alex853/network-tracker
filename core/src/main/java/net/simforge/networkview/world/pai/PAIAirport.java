package net.simforge.networkview.world.pai;

public class PAIAirport {
    private String icao;
    private double latitude;
    private double longitude;
    private int altitude;

    public PAIAirport(String icao, double latitude, double longitude, int altitude) {
        this.icao = icao;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getIcao() {
        return icao;
    }

    public int getAltitude() {
        return altitude;
    }
}

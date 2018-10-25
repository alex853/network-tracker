package net.simforge.networkview.world.airports;

import net.simforge.commons.misc.Geo;

public class AirportBuilder {
    private String icao;
    private Geo.Coords coords;
    private int elevation;
    private BoundaryType boundaryType;
    private String boundaryData;

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public Geo.Coords getCoords() {
        return coords;
    }

    public void setCoords(Geo.Coords coords) {
        this.coords = coords;
    }

    public int getElevation() {
        return elevation;
    }

    public void setElevation(int elevation) {
        this.elevation = elevation;
    }

    public BoundaryType getBoundaryType() {
        return boundaryType;
    }

    public void setBoundaryType(BoundaryType boundaryType) {
        this.boundaryType = boundaryType;
    }

    public String getBoundaryData() {
        return boundaryData;
    }

    public void setBoundaryData(String boundaryData) {
        this.boundaryData = boundaryData;
    }

    public Airport create() {
        return new Airport(icao, coords, elevation, boundaryType, boundaryData);
    }
}

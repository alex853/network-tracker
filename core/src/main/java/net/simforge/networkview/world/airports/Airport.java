package net.simforge.networkview.world.airports;

import net.simforge.commons.misc.Geo;

public class Airport {
    private String icao;
    private Geo.Coords coords;
    private int elevation;
    private BoundaryType boundaryType;
    private String boundaryData;

    private Geo.Coords[] circlesBoundaryCoords;
    private double[] circlesBoundaryRadiuses;

    Airport(String icao, Geo.Coords coords, int elevation, BoundaryType boundaryType, String boundaryData) {
        this.icao = icao;
        this.coords = coords;
        this.elevation = elevation;
        this.boundaryType = boundaryType != null ? boundaryType : BoundaryType.Default;
        this.boundaryData = boundaryData;
    }

    public String getIcao() {
        return icao;
    }

    public Geo.Coords getCoords() {
        return coords;
    }

    public int getElevation() {
        return elevation;
    }

    public boolean isWithinBoundary(Geo.Coords coords) {
        switch (boundaryType) {
            case Circles:
                return checkCirclesBoundary(coords);
            case Polygon:
                throw new UnsupportedOperationException("Polygon type of boundary is not supported");
            default:
                return checkDefaultBoundary(coords);
        }
    }

    private boolean checkDefaultBoundary(Geo.Coords coords) {
        return Geo.distance(this.coords, coords) <= 2.5;
    }

    private boolean checkCirclesBoundary(Geo.Coords coords) {
        parseCirclesBoundaryData();

        for (int i = 0; i < circlesBoundaryCoords.length; i++) {
            Geo.Coords center = circlesBoundaryCoords[i];
            double radius = circlesBoundaryRadiuses[i];
            double distance = Geo.distance(center, coords);
            if (distance <= radius) {
                return true;
            }
        }

        return false;
    }

    private void parseCirclesBoundaryData() {
        if (circlesBoundaryCoords != null) {
            return;
        }

        String[] lines = boundaryData.split("\n");
        int length = lines.length;
        circlesBoundaryCoords = new Geo.Coords[length];
        circlesBoundaryRadiuses = new double[length];

        for (int i = 0; i < length; i++) {
            String line = lines[i];
            try {
                String[] data = line.split(",");

                String latStr = data[0];
                String lonStr = data[1];
                String radiusStr = data[2];

                double lat = Double.parseDouble(latStr);
                double lon = Double.parseDouble(lonStr);
                double radius = Double.parseDouble(radiusStr);

                circlesBoundaryCoords[i] = new Geo.Coords(lat, lon);
                circlesBoundaryRadiuses[i] = radius;
            } catch (Exception e) {
                throw new IllegalStateException("Unable to parse '" + line + "' line as Circles boundary data in " + icao + " airport", e);
            }
        }
    }
}

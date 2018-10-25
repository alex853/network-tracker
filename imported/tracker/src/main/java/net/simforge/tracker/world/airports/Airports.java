package net.simforge.tracker.world.airports;

import net.simforge.commons.misc.Geo;

import java.io.IOException;
import java.util.*;

public class Airports {

    private Collection<Airport> airports = new LinkedList<>();

    @SuppressWarnings("unchecked")
    private List<Airport>[][] gridLL9 = (List<Airport>[][]) new List[360][180];

    public void addAirport(Airport airport) {
        airports.add(airport);

        putToGridLL9(airport);
    }

    public Airport getByIcao(String icao) {
        for (Airport airport : airports) {
            if (airport.getIcao().equals(icao)) {
                return airport;
            }
        }
        return null;
    }

    public Airport findNearest(Geo.Coords coords) {
        double lat = coords.getLat();
        double lon = coords.getLon();

        int latIndex = getLatIndex(lat);
        int lonIndex = getLonIndex(lon);

        //noinspection unchecked
        List<Airport> airports = gridLL9[lonIndex][latIndex];

        if (airports == null)
            return null;
        else
            return findNearest(coords, airports);
    }

    public Airport findWithinBoundary(Geo.Coords coords) {
        double lat = coords.getLat();
        double lon = coords.getLon();

        int latIndex = getLatIndex(lat);
        int lonIndex = getLonIndex(lon);

        //noinspection unchecked
        List<Airport> airports = gridLL9[lonIndex][latIndex];

        if (airports == null)
            return null;
        else
            return findWithinBoundary(coords, airports);
    }

    private Airport findNearest(Geo.Coords coords, Collection<Airport> airports) {
        double minDistance = Double.MAX_VALUE;
        Airport minDistanceAirport = null;

        for (Airport airport : airports) {
            double distance = Geo.distance(coords, airport.getCoords());
            if(distance < minDistance) {
                minDistance = distance;
                minDistanceAirport = airport;
            }
        }

        if (minDistanceAirport != null)
            return minDistanceAirport;
        else
            return null;
    }

    private Airport findWithinBoundary(Geo.Coords coords, List<Airport> airports) {
        for (Airport airport : airports) {
            if (airport.isWithinBoundary(coords)) {
                return airport;
            }
        }
        return null;
    }

    private void putToGridLL9(Airport airport) {
        double lat = airport.getCoords().getLat();
        double lon = airport.getCoords().getLon();

        for (int latI = -1; latI <= 1; latI++) {
            double latC = lat + latI;

            if (latC < -90 || latC >= 90)
                continue;

            for (int lonI = -1; lonI <= 1; lonI++) {
                double lonC = lon + lonI;

                if (lonC < -180) {
                    lonC = 360 + lonC;
                } else if (lonC >= 180) {
                    lonC = 360 - lonC;
                }

                putToGridLL(latC, lonC, gridLL9, airport);
            }
        }
    }

    private void putToGridLL(double lat, double lon, List<Airport>[][] gridLL, Airport airport) {
        int latIndex = getLatIndex(lat);
        int lonIndex = getLonIndex(lon);

        List<Airport> airportsLL = gridLL[lonIndex][latIndex];
        if (airportsLL == null) {
            airportsLL = new ArrayList<>();
            gridLL[lonIndex][latIndex] = airportsLL;
        }

        airportsLL.add(airport);
    }

    private int getLatIndex(double lat) {
        int latIndex = (int) (lat + 90);
        latIndex = latIndex % 180;
        return latIndex;
    }

    private int getLonIndex(double lon) {
        int lonIndex = (int) (lon + 180);
        lonIndex = lonIndex % 360;
        return lonIndex;
    }

    private static Airports airportsInstance;

    public static synchronized Airports get() {
        if (airportsInstance == null) {
            try {
                airportsInstance = AirportsLoader.load();
            } catch (IOException e) {
                throw new RuntimeException("Unable to load airports data", e);
            }
        }
        return airportsInstance;
    }
}

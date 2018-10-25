package net.simforge.tracker.world.pai;

import net.simforge.commons.misc.Geo;

import java.io.IOException;
import java.util.*;

public class PAIAirports {

    private static PAIAirports paiAirports = null;

    private Collection<PAIAirport> airports = new LinkedList<>();

    private Object[][] gridLL = new Object[360][180];
    private Object[][] gridLL9 = new Object[360][180];

    public PAIAirportAndDist findNearest(double lat, double lon) {
        return find(lat, lon, airports);
    }

    private PAIAirportAndDist find(double lat, double lon, Collection<PAIAirport> airports) {
        double minDistance = Double.MAX_VALUE;
        PAIAirport minDistanceAirport = null;

        for (PAIAirport airport : airports) {
            double distance = Geo.distance(new Geo.Coords(lat, lon), new Geo.Coords(airport.getLatitude(), airport.getLongitude()));
            if (distance < minDistance) {
                minDistance = distance;
                minDistanceAirport = airport;
            }
        }

        if (minDistanceAirport != null)
            return new PAIAirportAndDist(minDistanceAirport, minDistance);
        else
            return null;
    }

    public void addAirport(PAIAirport airport) {
        airports.add(airport);

        putToGridLL(airport, airport.getLatitude(), airport.getLongitude());
        putToGridLL9(airport, airport.getLatitude(), airport.getLongitude());
    }

    public Collection<PAIAirport> getAirports() {
        return Collections.unmodifiableCollection(airports);
    }

    private void putToGridLL(PAIAirport airport, double lat, double lon) {
        putToGridLL(lat, lon, gridLL, airport);
    }

    private void putToGridLL9(PAIAirport airport, double lat, double lon) {
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

    private void putToGridLL(double lat, double lon, Object[][] gridLL, PAIAirport airport) {
        int latIndex = getLatIndex(lat);
        int lonIndex = getLonIndex(lon);

        @SuppressWarnings("unchecked")
        List<PAIAirport> airportsLL = (List<PAIAirport>) gridLL[lonIndex][latIndex];
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

    public PAIAirportAndDist findNearestLL(double lat, double lon) {
        return findNearestLL(lat, lon, gridLL);
    }

    public PAIAirportAndDist findNearestLL9(double lat, double lon) {
        return findNearestLL(lat, lon, gridLL9);
    }

    private PAIAirportAndDist findNearestLL(double lat, double lon, Object[][] gridLL) {
        int latIndex = getLatIndex(lat);
        int lonIndex = getLonIndex(lon);

        @SuppressWarnings("unchecked")
        List<PAIAirport> airports = (List<PAIAirport>) gridLL[lonIndex][latIndex];

        if (airports == null)
            return null;
        else
            return find(lat, lon, airports);
    }

    public int findEglLL9(double lat, double lon) {
        int latIndex = getLatIndex(lat);
        int lonIndex = getLonIndex(lon);

        @SuppressWarnings("unchecked")
        List<PAIAirport> airports = (List<PAIAirport>) gridLL[lonIndex][latIndex];

        List<PAIAirportAndDist> result = new ArrayList<>();
        int resultSize = 3;

        if (airports != null) {
            for (PAIAirport airport : airports) {
                double distance = Geo.distance(new Geo.Coords(lat, lon), new Geo.Coords(airport.getLatitude(), airport.getLongitude()));
                boolean inserted = false;
                for (int i = 0; i < result.size(); i++) {
                    PAIAirportAndDist eachResult = result.get(i);
                    if (eachResult.getDistance() > distance) {
                        result.add(i, new PAIAirportAndDist(airport, distance));
                        inserted = true;
                        break;
                    }
                }

                if (!inserted)
                    result.add(new PAIAirportAndDist(airport, distance));

                while (result.size() > resultSize)
                    result.remove(result.size()-1);
            }
        }

        // generally speaking, this is not correct way
        if (!result.isEmpty()) {
            int sum = 0;
            for (PAIAirportAndDist airportAndDist : result) {
                sum += airportAndDist.getAirport().getAltitude();
            }
            return sum / result.size();
        }

        return 0; // if empty - sea level...
    }

    public PAIAirport getByIcao(String icao) {
        for (PAIAirport airport : airports) {
            if (airport.getIcao().equals(icao)) {
                return airport;
            }
        }
        return null;
    }


    public synchronized static PAIAirports get() {
        if (paiAirports == null) {
            try {
                paiAirports = PAIAirportsLoader.load();
            } catch (IOException e) {
                throw new RuntimeException("Can't load PAI data", e);
            }
        }
        return paiAirports;
    }
}

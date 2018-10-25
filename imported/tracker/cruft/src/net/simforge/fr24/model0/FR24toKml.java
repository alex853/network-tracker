package net.simforge.fr24.model0;

import forge.commons.Geo;
import forge.commons.db.DB;
import forge.commons.gc_kls2_com.GC;
import forge.commons.gc_kls2_com.GCAirport;
import net.simforge.commons.kml.KmlAltitudeMode;
import net.simforge.commons.kml.KmlOut;
import net.simforge.commons.logging.LogHelper;
import net.simforge.commons.misc.Str;
import net.simforge.commons.persistence.Persistence;
import org.joda.time.DateMidnight;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class FR24toKml {
    private static final Logger log = LogHelper.getLogger("FR24toKml");

    private static Map<String, Map<String, FRConnection>> byOrigin = new TreeMap<String, Map<String, FRConnection>>();

    public static void main(String[] args) throws SQLException, IOException {
        Connection connx = DB.getConnection();

        int lastId = 0;
        int count = 0;
        while (true) {
            List<FlightRadar24.Fr24> fr24List = Persistence.loadWhere(
                    connx,
                    FlightRadar24.Fr24.class,
                    String.format("id > %s order by id limit 10000", lastId));

            log.info("Loaded " + fr24List.size() + " records, already processed " + count);

            if (fr24List.isEmpty()) {
                break;
            }

            for (FlightRadar24.Fr24 fr24 : fr24List) {
                lastId = fr24.getId();

                count++;

                String type = fr24.getType();
                String originIata = fr24.getOriginIata();
                String destinationIata = fr24.getDestinationIata();
                String flightNumber = fr24.getFlightNumber();
                DateMidnight dof = fr24.getDt().toDateMidnight();

                if (Str.isEmpty(type)) {
                    continue;
                }

                //if (!type.startsWith("A34")) {
                //    continue;
                //}

                if (!type.startsWith("B77")) {
                    continue;
                }

                if (Str.isEmpty(originIata)) {
                    continue;
                }

                if (Str.isEmpty(destinationIata)) {
                    continue;
                }

                Map<String, FRConnection> byOrigin = FR24toKml.byOrigin.get(originIata);
                if (byOrigin == null) {
                    byOrigin = new TreeMap<String, FRConnection>();
                    FR24toKml.byOrigin.put(originIata, byOrigin);
                }

                FRConnection frConnection = byOrigin.get(destinationIata);
                if (frConnection == null) {
                    frConnection = new FRConnection(originIata, destinationIata);
                    byOrigin.put(destinationIata, frConnection);
                }

                frConnection.register(dof, flightNumber, type);
            }
        }

        log.info("Processing is complete, records count = " + count);

        FileOutputStream fis = new FileOutputStream("./fr24.kml");
        KmlOut out = new KmlOut(fis);
        out.openDocument();

        for (Map.Entry<String, Map<String, FRConnection>> byOriginEntry : FR24toKml.byOrigin.entrySet()) {
            String originIata = byOriginEntry.getKey();
            Map<String, FRConnection> byDestination = byOriginEntry.getValue();

            log.info("Origin " + originIata + ", destinations " + byDestination.size());

            out.openFolder();
            out.writeName(originIata);


            for (FRConnection frConnection : byDestination.values()) {
                String destinationIata = frConnection.getDestinationIata();

                log.info(originIata + " -> " + destinationIata);

                GCAirport originAirport = null;
                GCAirport destinationAirport = null;
                try {
                    originAirport = GC.findAirport(connx, originIata);
                    destinationAirport = GC.findAirport(connx, destinationIata);
                    connx.commit();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }

                if (originAirport == null || destinationAirport == null) {
                    continue;
                }

                out.openFolder();
                out.writeName(originIata + " -> " + destinationIata);

                out.openPlacemark();
                out.writeName("Route");

                out.openLineString();
                out.writeTesselate(true);
                out.writeAltitudeMode(KmlAltitudeMode.clampToGround);
                out.openCoordinates();

                calcLine(out, originAirport, destinationAirport);

                out.closeCoordinates();
                out.closeLineString();

                out.closePlacemark();

                out.openFolder();
                out.writeName("Flight numbers");
                for (String flightNumber : frConnection.getFlightNumbers()) {

                    out.openPlacemark();
                    out.writeName(flightNumber);
                    out.closePlacemark();

                }

                out.closeFolder();

                out.closeFolder();

            }

            out.closeFolder();
        }

        out.closeDocument();
        out.closeKml();

        fis.close();

        log.info("Finished");
        connx.close();
    }

    private static void calcLine(KmlOut out, GCAirport originAirport, GCAirport destinationAirport) throws IOException {
        out.writeCoordinates(originAirport.getLon(), originAirport.getLat(), 0);


        Geo.Coords currPoint = new Geo.Coords(originAirport.getLat(), originAirport.getLon());
        double currBearing = 0;
        double remainedDistance = Geo.distanceNM(currPoint.lat, currPoint.lon, destinationAirport.getLat(), destinationAirport.getLon());

        int distanceStep = 500;
        while (remainedDistance > distanceStep) {

            // determine direction
            double neg = getDist(currBearing - 1, currPoint, destinationAirport);
            double pos = getDist(currBearing + 1, currPoint, destinationAirport);

            double step;

            if (pos < neg) {
                step = 1;
            } else {
                step = -1;
            }

            double currDist = getDist(currBearing, currPoint, destinationAirport);
            double nextDist = getDist(currBearing + step, currPoint, destinationAirport);
            while (nextDist < currDist) {
                currDist = nextDist;
                currBearing += step;
                nextDist = getDist(currBearing + step, currPoint, destinationAirport);
            }

            Geo.Coords newPoint = Geo.pointByHdgNM(currPoint.lat, currPoint.lon, currBearing, distanceStep);

            out.writeCoordinates(newPoint.lon, newPoint.lat, 0);

            currPoint = newPoint;
            remainedDistance = Geo.distanceNM(currPoint.lat, currPoint.lon, destinationAirport.getLat(), destinationAirport.getLon());
        }

        out.writeCoordinates(destinationAirport.getLon(), destinationAirport.getLat(), 0);
    }

    private static double getDist(double currBearing, Geo.Coords currPoint, GCAirport destinationAirport) {
        Geo.Coords newPoint = Geo.pointByHdgNM(currPoint.lat, currPoint.lon, currBearing, 100);
        return Geo.distanceNM(destinationAirport.getLat(), destinationAirport.getLon(), newPoint.lat, newPoint.lon)
                + Geo.distanceNM(newPoint.lat, newPoint.lon, currPoint.lat, currPoint.lon);
    }

    public static class FRConnection {
        private String originIata;
        private String destinationIata;
        private Set<String> flightNumbers = new TreeSet<String>();

        public FRConnection(String originIata, String destinationIata) {
            this.originIata = originIata;
            this.destinationIata = destinationIata;
        }

        public void register(DateMidnight dof, String flightNumber, String type) {
            flightNumbers.add(flightNumber);
        }

        public String getDestinationIata() {
            return destinationIata;
        }

        public Set<String> getFlightNumbers() {
            return flightNumbers;
        }
    }
}

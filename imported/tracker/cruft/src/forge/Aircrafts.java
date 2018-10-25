package forge;

import forge.commons.db.DB;
import net.simforge.commons.persistence.Persistence;
import forge.commons.Misc;
import forge.commons.io.IOHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;

import entities.Movement;
import entities.Report;
import core.DBOps;

public class Aircrafts {
    private static Set<String> types = new HashSet<String>();

    public static void main(String[] args) throws SQLException, IOException {
        String typesStr = IOHelper.loadFile(new File("./data/aircraft_types.csv"));
        String[] strs = typesStr.split("\r\n");
        for (String str : strs) {
            String[] strs2 = str.split(";");
            String type = strs2[1];
            type = type.trim();
            if (type.equals("n/a") || type.equals("")) {
                continue;
            }
            types.add(type);
        }

        Connection connx = DB.getConnection();
        DB.executeUpdate(connx, "update movement set aircraft_id = null");
        DB.executeUpdate(connx, "truncate table aircraft");
        connx.close();

        Report curr = null;

        //noinspection InfiniteLoopStatement
        while (true) {
            if (curr == null) {
                curr = DBOps.findOldestReport();
            } else {
                Report report = DBOps.findNextReport(curr.getId());
                if (report == null) {
                    Misc.sleep(60000);
                    System.out.println("no reports");
                    continue;
                }
                curr = report;
            }

            connx = DB.getConnection();

            List<Movement> takeoffs = Persistence.loadWhere(connx, Movement.class, "dep_report_id = " + curr.getId());
            for (Movement takeoff : takeoffs) {
                String icao = takeoff.getDepIcao();
                if (icao != null) {
                    String type = takeoff.getAircraftType();
                    if (!types.contains(type)) {
                        type = "ZZZZ";
                    }

                    List<Aircraft> aircrafts = Persistence.loadWhere(connx, Aircraft.class, "type = '" + type + "' and icao = '" + icao + "'");
                    Aircraft aircraft = aircrafts.isEmpty() ? null : aircrafts.get(0);
                    if (aircraft == null) {
                        aircraft = new Aircraft();
                        aircraft.setType(type);
                        aircraft.setIcao(icao);
                        aircraft = Persistence.create(connx, aircraft);
                    }

                    aircraft.setIcao(null);
                    aircraft.setMovementId(takeoff.getId());
                    Persistence.update(connx, aircraft);

                    takeoff.setAircraftId(aircraft.getId());
                    Persistence.update(connx, takeoff);
                }
            }

            List<Movement> landings = Persistence.loadWhere(connx, Movement.class, "arr_report_id = " + curr.getId());
            for (Movement landing : landings) {
                Aircraft aircraft = Persistence.load(connx, Aircraft.class, landing.getAircraftId());
                if (aircraft != null) {
                    if (landing.getArrIcao() != null) {
                        aircraft.setIcao(landing.getArrIcao());
                        aircraft.setMovementId(0);
                        Persistence.update(connx, aircraft);
                    } else if (landing.getDepIcao() != null) {
                        aircraft.setIcao(landing.getDepIcao());
                        aircraft.setMovementId(0);
                        Persistence.update(connx, aircraft);
                    }
                }
            }

            connx.close();

            Misc.sleep(100);
        }
    }
}

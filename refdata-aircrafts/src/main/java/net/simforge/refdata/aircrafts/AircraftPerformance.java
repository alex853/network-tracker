package net.simforge.refdata.aircrafts;

import net.simforge.refdata.aircrafts.apdec.APDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

// future get max endurance
public class AircraftPerformance {
    private static final Logger logger = LoggerFactory.getLogger(AircraftPerformance.class);

    private static Map<String, Integer> missedCruiseIasByIcaoCode = Collections.synchronizedMap(new TreeMap<>());
    private static volatile long missedStatsPrintTs = System.currentTimeMillis();

    public static Integer getCruiseIas(String aircraftType) {
        Integer cruiseIas = APDatabase.getCruiseIas(aircraftType);
        if (cruiseIas != null) {
            return cruiseIas;
        }

        missedCruiseIasByIcaoCode.merge(aircraftType, 1, Integer::sum);
        long now = System.currentTimeMillis();
        if (now - missedStatsPrintTs >= 600000) {
            missedStatsPrintTs = now;
            StringBuilder sb = new StringBuilder("Missed getCruiseIas requests stats: \r\n");
            missedCruiseIasByIcaoCode.forEach((icao, freq) -> sb.append('\t').append(icao).append(" -> ").append(freq).append("\r\n"));
            logger.info(sb.toString());
        }

        return null;
    }

    private static Integer getCruiseIas_backup(String aircraftType) {
        if (aircraftType.startsWith("B7")
                || aircraftType.startsWith("A3")
                || aircraftType.equals("CRJ7")
                || aircraftType.equals("MD83")) {
            return 280;
        }

        switch (aircraftType) {
            case "AT72":
                return 185;
            case "DHC6":
                return 170;
            case "M20T":
                return 140;
        }

        return null;
    }
}

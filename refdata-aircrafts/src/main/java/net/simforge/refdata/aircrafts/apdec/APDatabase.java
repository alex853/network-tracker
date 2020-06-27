package net.simforge.refdata.aircrafts.apdec;

import com.google.common.base.Preconditions;
import net.simforge.atmosphere.Airspeed;
import net.simforge.commons.io.IOHelper;
import net.simforge.commons.legacy.misc.Settings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class APDatabase {
    private static Map<String, Integer> cruiseIasByIcaoCode = new HashMap<>();

    public static Integer getCruiseIas(String icaoCode) {
        icaoCode = remapIcaoCode(icaoCode);

        Integer cruiseIas = cruiseIasByIcaoCode.get(icaoCode);
        if (cruiseIas != null) {
            return cruiseIas != Integer.MIN_VALUE ? cruiseIas : null;
        }

        Parser parser = loadParser(icaoCode);
        if (parser == null) {
            cruiseIasByIcaoCode.put(icaoCode, Integer.MIN_VALUE);
            return null;
        }

        Integer cruiseTas = parser.getCruiseTas();
        Integer cruiseCeiling = parser.getCruiseCeiling();
        if (cruiseTas == null || cruiseCeiling == null) {
            cruiseIasByIcaoCode.put(icaoCode, Integer.MIN_VALUE);
            return null;
        }

        cruiseIas = Airspeed.tasToIas(cruiseTas, cruiseCeiling);
        cruiseIasByIcaoCode.put(icaoCode, cruiseIas);
        return cruiseIas;
    }

    private static Parser loadParser(String icaoCode) {
        String apdecRoot = Settings.get("refdata.airports.apdec.root");
        Preconditions.checkNotNull(apdecRoot, "APDEC root should be not specified in settings");
        File root = new File(apdecRoot);
        File dataFile = new File(root, icaoCode + "/data.html");
        if (!dataFile.exists()) {
            return null;
        }
        String content;
        try {
            content = IOHelper.loadFile(dataFile);
        } catch (IOException e) {
            return null;
        }
        return Parser.build(content);
    }

    // additional mapping like B789 -> B788
    private static String remapIcaoCode(String icaoCode) {
        return icaoCode;
    }
}

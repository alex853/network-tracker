package net.simforge.networkview.core.report;

import net.simforge.networkview.core.report.persistence.ReportPilotPosition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsingLogics {
    private static String[] callsignPatterns = {
            "[A-Z]{5}", // QWERT
            "[A-Z]-[A-Z]{4}", // Q-WERT
            "[A-Z]{2}-[A-Z]{3}", // QW-ERT
            "N[0-9]{3}[A-Z]{2}", // N999XX
            "[0-9]{5}", // 85123
            "RA[0-9]{5}", // RA85123
    };

    private static final String REG_PREFIX = "REG/";

    public static String parseRegNo(ReportPilotPosition position, String fpRemarks) {
        String reg = null;
        if (fpRemarks != null) {
            fpRemarks = fpRemarks.toUpperCase();
            int index = fpRemarks.indexOf(REG_PREFIX);
            if (index != -1) {
                String str = fpRemarks.substring(index + REG_PREFIX.length());
                reg = recognizeRegNo(str);
            }
        }

        if (reg == null) {
            reg = recognizeRegNo(position.getCallsign());
        }

        if (reg != null) {
            int i = reg.indexOf('-');
            if (i != -1) {
                reg = reg.substring(0, i) + reg.substring(i + 1);
            }
        }

        return reg;
    }

    public static String recognizeRegNo(String str) {
        if (str == null) {
            return null;
        }

        String reg = null;
        for (String eachPattern : callsignPatterns) {
            if (str.matches(eachPattern + "[ /+]?.*")) {
                Pattern pattern = Pattern.compile(eachPattern);
                Matcher matcher = pattern.matcher(str);
                //noinspection ResultOfMethodCallIgnored
                matcher.find();
                int s = matcher.start();
                int e = matcher.end();
                reg = str.substring(s, e);
            }
        }

        return reg;
    }

    public static String parseAircraftType(String fpAircraft) {
        if (fpAircraft == null) {
            return null;
        }

        int i = fpAircraft.indexOf("-");
        if (i != -1) {
            fpAircraft = fpAircraft.substring(0, i);
        }

        // We expect:
        //   [Z/]DDDD[/X] or [Z/]DDD[/X] or [Z/]DD[/X]
        // We split it by '/' symbol and the longest section will be aircraft type

        String[] strs = fpAircraft.split("/");
        String result = strs[0];
        for (i = 1; i < strs.length; i++) {
            if (result.length() < strs[i].length()) {
                result = strs[i];
            }
        }

        return result;
    }
}

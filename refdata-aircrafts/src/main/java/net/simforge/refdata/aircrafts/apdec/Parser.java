package net.simforge.refdata.aircrafts.apdec;

import com.google.common.base.Preconditions;
import net.simforge.commons.legacy.html.Html;

import java.util.ArrayList;
import java.util.List;

class Parser {
    private List<String> lines = new ArrayList<>();

    static Parser build(String content) {
        content = Html.toPlainText(content);
        String[] splitted = content.split("\r?\n");

        Parser parser = new Parser();
        for (String s : splitted) {
            s = s.trim();
            if (s.length() == 0) {
                continue;
            }
            parser.lines.add(s);
        }

        return parser;
    }

    Integer getCruiseTas() {
        int cruiseHeaderIndex = lines.indexOf("Cruise");
        Preconditions.checkState(cruiseHeaderIndex != -1, "Unable to find Cruise section");
        List<String> cruiseSection = lines.subList(cruiseHeaderIndex, cruiseHeaderIndex + 20);
        Preconditions.checkState("TAS".equals(cruiseSection.get(1)), "Cruise TAS - unexpected TAS header");
        String tasString = cruiseSection.get(2);
        Preconditions.checkState("kt".equals(cruiseSection.get(3)), "Cruise TAS - unexpected speed unit");
        try {
            return Integer.parseInt(tasString);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Cruise TAS - unable to parse Cruise TAS", e);
        }
    }

    Integer getCruiseCeiling() {
        int cruiseHeaderIndex = lines.indexOf("Cruise");
        Preconditions.checkState(cruiseHeaderIndex != -1, "Unable to find Cruise section");
        List<String> cruiseSection = lines.subList(cruiseHeaderIndex, cruiseHeaderIndex + 20);
        int cruiseCeilingHeaderIndex = cruiseSection.indexOf("Ceiling");
        Preconditions.checkState(cruiseCeilingHeaderIndex != -1, "Cruise Ceiling - unable to find Ceiling header");
        Preconditions.checkState("FL".equals(cruiseSection.get(cruiseCeilingHeaderIndex + 1)), "Cruise Ceiling - unexpected FL unit");
        String ceilingStr = cruiseSection.get(cruiseCeilingHeaderIndex + 2);
        try {
            return Integer.parseInt(ceilingStr) * 100;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Cruise Ceiling - unable to parse Cruise Ceiling", e);
        }
    }
}

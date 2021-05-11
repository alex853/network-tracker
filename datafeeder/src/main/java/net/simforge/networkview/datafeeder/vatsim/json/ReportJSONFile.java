package net.simforge.networkview.datafeeder.vatsim.json;

import com.google.gson.Gson;
import net.simforge.commons.misc.Str;
import net.simforge.networkview.core.Network;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ReportJSONFile {
    private static final int INT_NaN = Integer.MIN_VALUE;
    private static final float FLOAT_NaN = Float.NaN;
    private static final double DOUBLE_NaN = Double.NaN;

    private static final String FILE = "File";
    private static final String CLIENTS = "Clients";

    private static final String DOES_NOT_MATTER = "!!@@##DOES_NOT_MATTER##@@!!";
    private static final String END_NOT_FOUND = "END not found";

    private Network network;
    private String update;

//    private Map<String, Section> sections = new HashMap<>();
//    private List<LogEntry> log = new ArrayList<>();
//
//    private List<ClientInfo> clientInfos;

    private Gson gson = new Gson();

    public ReportJSONFile(Network network, String data) {
        this.network = network;

        Map map = gson.fromJson(data, Map.class);
        Map general = (Map) map.get("general");
        update = (String) general.get("update");
    }

    public String getUpdate() {
        return update;
    }
}

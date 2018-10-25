package net.simforge.networkview.world.pai;

import net.simforge.commons.io.IOHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class PAIAirportsLoader {
    public static PAIAirports load() throws IOException {
        PAIAirports airports = new PAIAirports();

        InputStream paiDataInputStream = PAIAirportsLoader.class.getResourceAsStream("/net/simforge/tracker/world/pai/Airports_PAI.txt");
        String content = IOHelper.readInputStream(paiDataInputStream);

        StringTokenizer st = new StringTokenizer(content, "\r\n");
        while(st.hasMoreTokens()) {
            String nextToken = st.nextToken();

            try {
                StringTokenizer st2 = new StringTokenizer(nextToken, ",");

                String icao = st2.nextToken();
                String latStr = st2.nextToken();
                String lonStr = st2.nextToken();
                String altStr = st2.nextToken();

                st2 = null;

                double latitude = strCoordToCoord(latStr);
                double longitude = strCoordToCoord(lonStr);

                PAIAirport airport = new PAIAirport(icao, latitude, longitude, Integer.parseInt(altStr));
                airports.addAirport(airport);
            } catch(NoSuchElementException e) {

            }
        }

        return airports;
    }

    private static double strCoordToCoord(String str) {
        str = str.trim();
        char c = str.charAt(0);
        double sign = 0;
        if((c == 'N') || (c == 'E'))
            sign = 1;
        else
        if((c == 'S') || (c == 'W'))
            sign = -1;

        int index = str.indexOf("* ");
        String str1 = str.substring(1, index);
        double v1 = Integer.parseInt(str1);

        String str2 = str.substring(index+2);
        if(str2.endsWith("\'"))
            str2 = str2.substring(0, str2.length()-1);
        double v2 = 0;
        try {
            v2 = Double.parseDouble(str2.replace('.', ','));
        } catch(NumberFormatException e) {
            v2 = Double.parseDouble(str2.replace(',', '.'));
        }

        return sign * (v1 + v2 / 60);
    }
}

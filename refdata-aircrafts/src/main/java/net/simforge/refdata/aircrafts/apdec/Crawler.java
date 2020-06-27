package net.simforge.refdata.aircrafts.apdec;

import com.google.common.base.Preconditions;
import net.simforge.commons.io.IOHelper;
import net.simforge.commons.legacy.misc.Settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Crawler {
    public static void main(String[] args) throws IOException, InterruptedException {
        // making list of ICAO codes
        String content = IOHelper.download("https://contentzone.eurocontrol.int/aircraftperformance/details.aspx?ICAO=A320");
        int indexFrom = content.indexOf("ctl00$MainContent$wsGroupDropDownList");
        int indexTo = content.indexOf("</select>", indexFrom);
        String icaoCodesSelectTag = content.substring(indexFrom, indexTo);
        List<String> icaoCodes = new ArrayList<>();
        while (true) {
            int index = icaoCodesSelectTag.indexOf("<option");
            if (index == -1) {
                break;
            }
            index = icaoCodesSelectTag.indexOf("value=\"", index);
            indexFrom = icaoCodesSelectTag.indexOf("\"", index) + 1;
            indexTo = icaoCodesSelectTag.indexOf("\"", indexFrom);
            String icao = icaoCodesSelectTag.substring(indexFrom, indexTo);
            icaoCodes.add(icao);
            icaoCodesSelectTag = icaoCodesSelectTag.substring(indexTo);
        }
        System.out.println("Loaded " + icaoCodes.size() + " ICAO codes");

        String apdecRoot = Settings.get("refdata.airports.apdec.root");
        Preconditions.checkNotNull(apdecRoot, "APDEC root should be not specified in settings");
        File root = new File(apdecRoot);
        for (String icaoCode : icaoCodes) {
            System.out.println("ICAO " + icaoCode);
            content = IOHelper.download("https://contentzone.eurocontrol.int/aircraftperformance/details.aspx?ICAO=" + icaoCode);
            File folder = new File(root, icaoCode);
            folder.mkdirs();
            IOHelper.saveFile(new File(folder, "data.html"), content);

            int index = content.indexOf("<img id=\"MainContent_wsDrawing\"");
            index = content.indexOf("src=\"", index);
            indexFrom = content.indexOf("\"", index) + 1;
            indexTo = content.indexOf("\"", indexFrom);
            String imageURL = content.substring(indexFrom, indexTo);
            if (!imageURL.endsWith(".gif")) {
                throw new IllegalArgumentException("Unknown image extension: " + imageURL);
            }
            URL url = new URL(imageURL);
            try {
                InputStream is = url.openConnection().getInputStream();
                FileOutputStream fos = new FileOutputStream(new File(folder, "drawing.gif"));
                byte[] buf = new byte[1000];
                while (true) {
                    int read = is.read(buf);
                    if (read == -1) {
                        break;
                    }
                    fos.write(buf, 0, read);
                }
                is.close();
                fos.close();
            } catch (IOException e) {
                System.out.println("Error on image loading");
            }

            Thread.sleep(1000);
        }
    }
}

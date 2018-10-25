package tracker;

import forge.commons.Settings;
import forge.commons.io.IOHelper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.File;

public class TrackerKml extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String root = Settings.get("webapps.tracker-2.root");

        String path = getServletContext().getRealPath("tracker-src.kml");
        String kml = IOHelper.loadFile(new File(path));
        kml = kml.replaceAll("#root#", root);

        response.setHeader("Content-Type", "application/vnd.google-earth.kml+xml");
        ServletOutputStream outStr = response.getOutputStream();
        outStr.print(kml);
    }
}

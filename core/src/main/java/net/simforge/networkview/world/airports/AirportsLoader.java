package net.simforge.networkview.world.airports;

import net.simforge.commons.misc.Geo;
import net.simforge.networkview.world.pai.PAIAirport;
import net.simforge.networkview.world.pai.PAIAirports;
import net.simforge.networkview.world.pai.PAIAirportsLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

public class AirportsLoader {
    public static Airports load() throws IOException {
        Airports airports = new Airports();

        PAIAirports paiAirports = PAIAirportsLoader.load();
        Collection<PAIAirport> paiAirportsCollection = paiAirports.getAirports();

        for (PAIAirport paiAirport : paiAirportsCollection) {
            AirportBuilder builder = new AirportBuilder();

            builder.setIcao(paiAirport.getIcao());
            builder.setCoords(new Geo.Coords(paiAirport.getLatitude(), paiAirport.getLongitude()));
            builder.setElevation(paiAirport.getAltitude());

            String resourceName = "/boundaries/%s.properties";
            resourceName = resourceName.replace("%s", paiAirport.getIcao());
            InputStream resourceInputStream = PAIAirportsLoader.class.getResourceAsStream(resourceName);

            if (resourceInputStream != null) {
                Properties properties = new Properties();
                properties.load(resourceInputStream);

                String type = properties.getProperty("type");
                String data = properties.getProperty("data");

                builder.setBoundaryType(BoundaryType.valueOf(type));
                builder.setBoundaryData(data);
            }

            Airport airport = builder.create();

            airports.addAirport(airport);
        }

        return airports;
    }
}

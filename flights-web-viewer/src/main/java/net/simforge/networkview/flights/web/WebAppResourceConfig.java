package net.simforge.networkview.flights.web;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("rest")
public class WebAppResourceConfig extends ResourceConfig {
    public WebAppResourceConfig() {
        register(JacksonFeature.class);

        packages("net.simforge.networkview.flights.web");
    }
}

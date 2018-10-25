package net.simforge.tracker.webapp;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("rest")
public class WebAppResourceConfig extends ResourceConfig {
    public WebAppResourceConfig() {
        register(JacksonFeature.class);

        packages(
                "net.simforge.tracker.webapp.rest",
                "net.simforge.tracker.webapp.tracking2015.rest");
    }
}

package net.simforge.networkview.flights;

import net.simforge.networkview.flights.persistence.Flight;
import net.simforge.commons.hibernate.SessionFactoryBuilder;
import org.hibernate.SessionFactory;

public class FlightRecognition {
    public static final Class[] entities = {
            Flight.class
    };

    public static SessionFactory buildSessionFactory() {
        return SessionFactoryBuilder
                .forDatabase("net/simforge/networkview/flights")
                .entities(entities)
                .build();
    }
}

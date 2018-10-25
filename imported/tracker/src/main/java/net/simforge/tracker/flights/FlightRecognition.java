package net.simforge.tracker.flights;

import net.simforge.commons.hibernate.SessionFactoryBuilder;
import net.simforge.tracker.flights.persistence.Flight;
import org.hibernate.SessionFactory;

public class FlightRecognition {
    public static final Class[] entities = {
            Flight.class
    };

    public static SessionFactory buildSessionFactory() {
        return SessionFactoryBuilder
                .forDatabase("flights")
                .entities(entities)
                .build();
    }
}

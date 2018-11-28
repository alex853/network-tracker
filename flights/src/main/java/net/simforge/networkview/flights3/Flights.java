package net.simforge.networkview.flights3;

import net.simforge.commons.hibernate.SessionFactoryBuilder;
import net.simforge.networkview.flights3.persistence.DBEvent;
import net.simforge.networkview.flights3.persistence.DBFlight;
import net.simforge.networkview.flights3.persistence.DBPilotStatus;
import org.hibernate.SessionFactory;

public class Flights {
    public static final Class[] entities = {
            DBPilotStatus.class,
            DBFlight.class,
            DBEvent.class
    };

    public static SessionFactory buildSessionFactory(String databaseName) {
        return SessionFactoryBuilder
                .forDatabase(databaseName)
                .entities(entities)
                .build();
    }

    public static SessionFactory buildSessionFactoryWithSchema(String databaseName) {
        return SessionFactoryBuilder
                .forDatabase(databaseName)
                .entities(entities)
                .createSchemaIfNeeded()
                .build();
    }

}

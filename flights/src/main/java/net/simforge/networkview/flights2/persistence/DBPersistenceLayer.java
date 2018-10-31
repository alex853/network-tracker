package net.simforge.networkview.flights2.persistence;

import net.simforge.commons.hibernate.HibernateUtils;
import net.simforge.commons.legacy.BM;
import net.simforge.networkview.flights2.PersistenceLayer;
import net.simforge.networkview.flights2.PilotContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class DBPersistenceLayer implements PersistenceLayer {
    private SessionFactory sessionFactory; // todo
    
    @Override
    public PilotContext createContext(int pilotNumber) {
        BM.start("DBPersistenceLayer.createContext");
        try (Session session = sessionFactory.openSession()) {
            DBPilotStatus dbPilotStatus = loadPilotStatus(session, pilotNumber);
            
            if (dbPilotStatus != null) {
                throw new IllegalArgumentException("Pilot status for pilot " + pilotNumber + " already exists");
            }

            dbPilotStatus = new DBPilotStatus();
            dbPilotStatus.setPilotNumber(pilotNumber);
            
            HibernateUtils.saveAndCommit(session, dbPilotStatus);
            
            return new PilotContext(pilotNumber);
        } finally {
            BM.stop();
        }
    }

    @Override
    public PilotContext loadContext(int pilotNumber) {
        BM.start("DBPersistenceLayer.loadContext");
        try (Session session = sessionFactory.openSession()) {
            DBPilotStatus dbPilotStatus = loadPilotStatus(session, pilotNumber);

            if (dbPilotStatus == null) {
                throw new IllegalArgumentException("Pilot status for pilot " + pilotNumber + " is not found");
            }

            // todo load last flights
            // todo load last

            // todo
            return new PilotContext(pilotNumber);
        } finally {
            BM.stop();
        }
    }

    @Override
    public PilotContext saveChanges(PilotContext pilotContext) {
        throw new UnsupportedOperationException("DBPersistenceLayer.saveChanges");
    }

    private DBPilotStatus loadPilotStatus(Session session, int pilotNumber) {
        return (DBPilotStatus) session.createQuery("select ps from PilotStatus ps where pilotNumber = :pilotNumber")
                .setInteger("pilotNumber", pilotNumber)
                .uniqueResult();
    }
}

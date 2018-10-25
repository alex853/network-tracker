package web;

import entities.Pilot;
import entities.Report;
import entities.ReportFpRemarks;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.Connection;
import java.sql.SQLException;

import net.simforge.commons.persistence.Persistence;
import net.simforge.commons.persistence.BaseEntity;
import forge.commons.db.DB;
import net.simforge.commons.logging.LogHelper;
import org.joda.time.DateTimeConstants;

public class WebCache {
    private static Logger logger = LogHelper.getLogger("WebCache");
    private static long lastSweep;

    private static final Map<Integer, CacheEntry> pilots = new HashMap<Integer, CacheEntry>();
    private static final Map<Integer, CacheEntry> reports = new HashMap<Integer, CacheEntry>();
    private static final Map<Integer, CacheEntry> fpRemarks = new HashMap<Integer, CacheEntry>();

    public static Pilot getPilot(int pilotId) {
        return (Pilot) get(pilotId, Pilot.class, pilots);
    }

    public static Report getReport(int reportId) {
        return (Report) get(reportId, Report.class, reports);
    }

    public static ReportFpRemarks getFpRemarks(int fpRemarksId) {
        if (fpRemarksId == 0) {
            return null;
        }
        return (ReportFpRemarks) get(fpRemarksId, ReportFpRemarks.class, fpRemarks);
    }

    private static BaseEntity get(int entityId, Class clazz, Map<Integer, CacheEntry> cacheEntryMap) {
        sweep();
        CacheEntry entry = cacheEntryMap.get(entityId);
        if (entry == null) {
            BaseEntity entity = null;
            try {
                Connection connx = DB.getConnection();
                entity = Persistence.load(connx, clazz, entityId);
                connx.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Could not load " + clazz, e);
            }
            entry = new CacheEntry(entity);
            cacheEntryMap.put(entityId, entry);
        }
        entry.updateLastUsed();
        return (BaseEntity) entry.get();
    }

    private static void sweep() {
        if (System.currentTimeMillis() - lastSweep > 60000) {
            sweepCache(pilots, "Pilots");
            sweepCache(reports, "Reports");
            sweepCache(fpRemarks, "FpRemarks");
        }
        lastSweep = System.currentTimeMillis();
    }

    private static void sweepCache(Map<Integer, CacheEntry> map, String name) {
        synchronized (map) {
            List<Integer> expired = new ArrayList<Integer>();
            for (Map.Entry<Integer, CacheEntry> entry : map.entrySet()) {
                if (entry.getValue().isExpired()) {
                    expired.add(entry.getKey());
                }
            }
            for (Integer id : expired) {
                map.remove(id);
            }
            logger.info(name + ": size " + map.size() + " (expired " + expired.size() + ")");
        }
    }

    private static class CacheEntry {
        private Object value;
        private long lastUsed;

        private CacheEntry(Object value) {
            this.value = value;
            lastUsed = System.currentTimeMillis();
        }

        public void updateLastUsed() {
            lastUsed = System.currentTimeMillis();
        }

        public Object get() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - lastUsed > 10*DateTimeConstants.MILLIS_PER_MINUTE;
        }
    }
}

package net.simforge.tracker.webapp;

import net.simforge.tracker.Network;
import net.simforge.tracker.SessionManager;
import net.simforge.tracker.webapp.dto.NetworkStatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebAppContext {

    private static final Logger log = LoggerFactory.getLogger(WebAppContext.class.getName());

    private static final String ATTRIBUTE_NAME = WebAppContext.class.getName();

    private final SessionManager sessionManager;
    private final ScheduledExecutorService scheduler;

    private Map<Network, NetworkStatusDto> networkStatuses = new HashMap<>();
    private Map<String, byte[]> iconCache = new HashMap<>();

    public static void create(ServletContext servletContext) {
        servletContext.setAttribute(ATTRIBUTE_NAME, new WebAppContext());
    }

    public static WebAppContext get(ServletContext servletContext) {
        return (WebAppContext) servletContext.getAttribute(ATTRIBUTE_NAME);
    }

    private WebAppContext() {
        log.info("creating session manager");

        sessionManager = new SessionManager();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new NetworkStatusRefresher(this, Network.VATSIM), 0, 60, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(new NetworkStatusRefresher(this, Network.IVAO), 0, 60, TimeUnit.SECONDS);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public synchronized void destroy() {
        log.info("disposing session manager");

        sessionManager.dispose();

        scheduler.shutdownNow();
    }

    public synchronized NetworkStatusDto getNetworkStatus(Network network) {
        return networkStatuses.get(network);
    }

    public synchronized void setNetworkStatus(Network network, NetworkStatusDto networkStatus) {
        networkStatuses.put(network, networkStatus);
    }

    public Map<String, byte[]> getIconCache() {
        return iconCache;
    }
}

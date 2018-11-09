package net.simforge.networkview.flights.web;

import net.simforge.commons.misc.Misc;
import net.simforge.commons.misc.RestUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@Deprecated
//@Path("aircraft-models")
public class AircraftModelsAPI {
/*    private static final Logger logger = LoggerFactory.getLogger(AircraftModelsAPI.class.getName());

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private ServletContext servletContext;
    private WebAppContext webAppContext;

    private AuthHelper auth;

    @Context
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        this.webAppContext = WebAppContext.get(servletContext);
    }

    @Context
    public void setRequest(HttpServletRequest request) {
        this.auth = new AuthHelper(request);
    }

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws IOException, SQLException {
        logger.debug("loading list of aircraft models");

        try (Session session = webAppContext.openSession()) {
            auth.checkLoggedIn();

            //noinspection JpaQlInspection,unchecked
            List<AircraftMakeModel> data = session
                    .createQuery("from AircraftMakeModel")
                    .list();

            Map<Integer, ModelInfo> roots = new HashMap<>();
            Map<Integer, ModelInfo> id2info = new HashMap<>();

            List<AircraftMakeModel> remained = new ArrayList<>(data);

            // TODO: 04.02.2017 iterate it several times!!! while remained contains somewhat or no changes happen
            Iterator<AircraftMakeModel> it = remained.iterator();
            while (it.hasNext()) {
                AircraftMakeModel model = it.next();

                AircraftMakeModel parent = model.getParent();
                ModelInfo info;
                if (parent != null) {
                    ModelInfo parentInfo = id2info.get(parent.getId());
                    if (parentInfo == null) {
                        continue; // need to wait for initialization of info for parent model
                    }

                    info = new ModelInfo(model, parentInfo);
                } else {
                    info = new ModelInfo(model);

                    roots.put(model.getId(), info);
                }

                id2info.put(model.getId(), info);
                it.remove();
            }

            List<Object> dtos = new ArrayList<>();

            List<ModelInfo> rootInfos = new ArrayList<>(roots.values());
            rootInfos.sort(ModelInfo::compareByName);
            for (ModelInfo rootInfo : rootInfos) {
                addDtos(dtos, rootInfo);
            }

            return Response.ok(RestUtils.success(dtos)).build();
        } catch (Exception e) {
            logger.error("Could not load aircraft models", e);

            String msg = String.format("Could not load aircraft models: %s", Misc.messagesBr(e));
            return Response.ok(RestUtils.failure(msg)).build();
        }
    }

    private void addDtos(List<Object> dtos, ModelInfo info) {
        dtos.add(toDto(info));

        List<ModelInfo> children = new ArrayList<>(info.children);
        children.sort(ModelInfo::compareByName);

        for (ModelInfo child : children) {
            addDtos(dtos, child);
        }
    }

    private Object toDto(ModelInfo info) {
        return new Object() {
            public String getId() {
                return String.valueOf(info.model.getId());
            }

            public String getLevel() {
                return String.valueOf(info.level);
            }

            public String getLevelPx() {
                return String.valueOf(info.level * 20);
            }

            public String getName() {
                return info.model.getName();
            }

            public String getIcao() {
                return info.model.getIcao();
            }

        };
    }

    private static class ModelInfo {
        private AircraftMakeModel model;
        private int level;
        private ModelInfo parentInfo;
        private Collection<ModelInfo> children = new ArrayList<>();

        public ModelInfo(AircraftMakeModel model) { // root element
            this.model = model;
            this.level = 0;
        }

        public ModelInfo(AircraftMakeModel model, ModelInfo parentInfo) {
            this.model = model;
            this.parentInfo = parentInfo;
            this.level = parentInfo.level + 1;
            this.parentInfo.children.add(this);
        }

        public static int compareByName(ModelInfo info1, ModelInfo info2) {
            return info1.model.getName().compareTo(info2.model.getName());
        }
    }*/
}

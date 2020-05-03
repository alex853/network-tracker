package net.simforge.networkview.flights.processor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.core.report.ReportInfoDto;
import net.simforge.networkview.datafeeder.ReportInfo;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Component
public class FlightPersistenceService_GsonLocalImpl implements FlightPersistenceService {
    private File storagePath;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @PostConstruct
    public void init() {
        String gsonLocalStorage = System.getProperty("gson-local-storage");
        if (gsonLocalStorage == null) {
            throw new IllegalArgumentException();
        }
        storagePath = new File(gsonLocalStorage);
    }

    @Override
    public ReportInfo loadLastProcessedReport() {
        File reportFile = new File(storagePath, "$last-processed-report.json");
        if (!reportFile.exists()) {
            return null;
        }
        String json;
        try {
            json = IOHelper.loadFile(reportFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return gson.fromJson(json, ReportInfoDto.class);
    }

    @Override
    public void saveLastProcessedReport(ReportInfo report) {
        File reportFile = new File(storagePath, "$last-processed-report.json");
        reportFile.getParentFile().mkdirs();
        String json = gson.toJson(report);
        try {
            IOHelper.saveFile(reportFile, json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PilotContextDto loadPilotContextInfo(int pilotNumber) {
        File pilotContextFile = new File(storagePath, "pilot-" + pilotNumber + "/" + "$pilot-context.json");
        if (!pilotContextFile.exists()) {
            return null;
        }
        String json;
        try {
            json = IOHelper.loadFile(pilotContextFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return gson.fromJson(json, PilotContextDto.class);
    }

    @Override
    public void upsertPilotContextInfo(PilotContextDto pilotContext) {
        File pilotContextFile = new File(storagePath, "pilot-" + pilotContext.getPilotNumber() + "/" + "$pilot-context.json");
        pilotContextFile.getParentFile().mkdirs();
        String json = gson.toJson(pilotContext);
        try {
            IOHelper.saveFile(pilotContextFile, json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void upsertFlight(FlightDto flight) {
        File flightFile = new File(storagePath, "pilot-" + flight.getPilotNumber() + "/" + flight.getFirstSeen().getReportInfo().getReport() + ".json");
        flightFile.getParentFile().mkdirs();
        String json = gson.toJson(flight);
        try {
            IOHelper.saveFile(flightFile, json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package net.simforge.tracker.datafeeder;

import net.simforge.commons.io.IOHelper;
import net.simforge.commons.legacy.BM;
import net.simforge.tracker.Network;
import net.simforge.tracker.tools.ReportUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReportStorage {

    public static final String DEFAULT_STORAGE_ROOT = "../data";

    private static DateTimeFormatter yyyy = DateTimeFormat.forPattern("yyyy").withZoneUTC();
    private static DateTimeFormatter yyyyMM = DateTimeFormat.forPattern("yyyy-MM").withZoneUTC();
    private static DateTimeFormatter yyyyMMdd = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC();

    private File root;
    private Network network;

    private ReportStorage(String rootPath, Network network) {
        this.network = network;

        root = new File(rootPath + "/" + network.name());
        //noinspection ResultOfMethodCallIgnored
        root.mkdirs();
    }

    public static ReportStorage getStorage(String storageRoot, Network network) {
        return new ReportStorage(storageRoot, network);
    }

    public File getRoot() {
        return root;
    }

    public void saveReport(String report, String data) throws IOException {
        String filename = reportToFullPath(report);
        File file = new File(root, filename);
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        IOHelper.saveFile(file, data);
    }

    private String reportToFullPath(String report) {
        DateTime dateTime = ReportUtils.fromTimestamp(report);
        return yyyy.print(dateTime) + "/" + yyyyMM.print(dateTime) + "/" + yyyyMMdd.print(dateTime) + "/" + report + ".txt";
    }

    public String getFirstReport() throws IOException {
        List<String> allReports = listAllReportFiles();
        if (allReports.size() == 0) {
            return null;
        }
        return allReports.get(0);
    }

    public String getNextReport(String previousReport) throws IOException {
        List<String> allReports = listAllReportFiles();
        if (allReports.size() == 0) {
            return null;
        }
        int index = allReports.indexOf(previousReport);
        if (index == -1) {
            return null;
        }
        if (index == allReports.size() - 1) {
            return null;
        }
        return allReports.get(index + 1);
    }

    public String getLastReport() throws IOException {
        List<String> allReports = listAllReportFiles();
        if (allReports.size() == 0) {
            return null;
        }
        return allReports.get(allReports.size() - 1);
    }

    public ReportFile getReportFile(String report) throws IOException {
        String filename = reportToFullPath(report);
        File file = new File(root, filename);
        String content = IOHelper.loadFile(file);
        //noinspection UnnecessaryLocalVariable
        ReportFile reportFile = new ReportFile(network, content);
        return reportFile;
    }

    private List<String> listAllReportFiles() throws IOException {
        BM.start("ReportStorage.listAllReportFiles");
        try {
            final List<String> reports = new ArrayList<>();

            Files.walkFileTree(Paths.get(root.toURI()), new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!attrs.isRegularFile()) {
                        return FileVisitResult.CONTINUE;
                    }

                    String filename = file.getFileName().toString();
                    if (!filename.endsWith(".txt")) {
                        return FileVisitResult.CONTINUE;
                    }

                    filename = filename.substring(0, filename.length() - ".txt".length());

                    if (!ReportUtils.isTimestamp(filename)) {
                        return FileVisitResult.CONTINUE;
                    }

                    reports.add(filename);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });

            Collections.sort(reports);

            return reports;
        } finally {
            BM.stop();
        }
    }
}

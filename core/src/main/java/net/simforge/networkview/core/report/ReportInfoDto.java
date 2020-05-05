package net.simforge.networkview.core.report;

public class ReportInfoDto implements ReportInfo {
    private final Long id;
    private final String report;

    public ReportInfoDto(Long id, String report) {
        this.id = id;
        this.report = report;
    }

    public ReportInfoDto(ReportInfo reportInfo) {
        this.id = reportInfo.getId();
        this.report = reportInfo.getReport();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getReport() {
        return report;
    }

    @Override
    public String toString() {
        return "ReportInfoDto{" +
                "id=" + id +
                ", report='" + report + '\'' +
                '}';
    }
}

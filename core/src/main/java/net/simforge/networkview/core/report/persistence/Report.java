package net.simforge.networkview.core.report.persistence;

import net.simforge.networkview.core.report.ReportInfo;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "report")
public class Report implements Serializable, ReportInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pk_report_id")
    @SequenceGenerator(name = "pk_report_id", sequenceName = "report_id_seq", allocationSize = 1)
    private Long id;
    @Version
    private Integer version;

    private String report;
    private Integer clients;
    private Integer pilots;
    @Column(name = "has_logs")
    private Boolean hasLogs;
    private Boolean parsed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public Integer getClients() {
        return clients;
    }

    public void setClients(Integer clients) {
        this.clients = clients;
    }

    public Integer getPilots() {
        return pilots;
    }

    public void setPilots(Integer pilots) {
        this.pilots = pilots;
    }

    public Boolean getHasLogs() {
        return hasLogs;
    }

    public void setHasLogs(Boolean hasLogs) {
        this.hasLogs = hasLogs;
    }

    public Boolean getParsed() {
        return parsed;
    }

    public void setParsed(Boolean parsed) {
        this.parsed = parsed;
    }

    @Override
    public String toString() {
        return "Report{" +
                "id=" + id +
                ", report='" + report + '\'' +
                '}';
    }
}

package net.simforge.networkview.core.report.persistence;

import javax.persistence.*;

@Entity
@Table(name = "report_log")
public class ReportLogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pk_report_log_id")
    @SequenceGenerator(name = "pk_report_log_id", sequenceName = "report_log_id_seq", allocationSize = 1)
    private Long id;
    @Version
    private Integer version;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;
    //@Cut(size = 50)
    private String section;
    //@Cut(size = 50)
    private String object;
    //@Cut(size = 200)
    private String message;
    //@Cut(size = 1000)
    private String value;

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

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

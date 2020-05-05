package net.simforge.networkview.core.report.persistence;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "report_pilot_fp_remarks")
public class ReportPilotFpRemarks implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pk_report_pilot_fp_remarks_id")
    @SequenceGenerator(name = "pk_report_pilot_fp_remarks_id", sequenceName = "report_pilot_fp_remarks_id_seq", allocationSize = 1)
    private Long id;
    @Version
    private Integer version;

   // @Cut(size = 300)
    private String remarks;

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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}

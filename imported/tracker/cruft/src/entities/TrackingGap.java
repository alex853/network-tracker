package entities;

import org.joda.time.DateTime;
import net.simforge.commons.persistence.Column;
import net.simforge.commons.persistence.BaseEntity;
import net.simforge.commons.persistence.Table;

@Table(name = "tracking_gap")
public class TrackingGap extends BaseEntity {
    @Column
    private DateTime since;

    @Column
    private DateTime till;

    public DateTime getSince() {
        return since;
    }

    public void setSince(DateTime since) {
        this.since = since;
    }

    public DateTime getTill() {
        return till;
    }

    public void setTill(DateTime till) {
        this.till = till;
    }
}

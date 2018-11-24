package net.simforge.networkview.datafeeder;

import java.time.LocalDateTime;

public interface ReportInfo {
    Long getId();

    String getReport();

    default LocalDateTime getDt() {
        return ReportUtils.fromTimestampJava(getReport());
    }
}

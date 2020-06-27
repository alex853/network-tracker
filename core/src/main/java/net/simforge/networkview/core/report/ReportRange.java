package net.simforge.networkview.core.report;

public class ReportRange {
    private final ReportInfo since;
    private final ReportInfo till;

    private ReportRange(ReportInfo since, ReportInfo till) {
        this.since = since;
        this.till = till;
    }

    public static ReportRange between(ReportInfo since, ReportInfo till) {
        if (!ReportUtils.isTimestampLessOrEqual(since.getReport(), till.getReport())) {
            throw new IllegalArgumentException(String.format("Since report has to be less or equal till report - since %s, till %s", since.getReport(), till.getReport()));
        }

        return new ReportRange(new ReportInfoDto(since), new ReportInfoDto(till));
    }

    public ReportInfo getSince() {
        return since;
    }

    public ReportInfo getTill() {
        return till;
    }

    /**
     * -[======]----
     * ----[=====]--
     * <p>
     * -[========]--
     * ---[===]-----
     * <p>
     * -[===]-------
     * -------[==]--
     */
    public ReportRange intersect(ReportRange anotherRange) {
        ReportInfo maxSince = ReportUtils.isTimestampGreater(anotherRange.getSince().getReport(), since.getReport())
                ? anotherRange.getSince() : since;
        ReportInfo minTill = ReportUtils.isTimestampLess(anotherRange.getTill().getReport(), till.getReport())
                ? anotherRange.getTill() : till;

        if (ReportUtils.isTimestampLess(minTill.getReport(), maxSince.getReport())) {
            return null;
        } else {
            return ReportRange.between(maxSince, minTill);
        }
    }

    public boolean isWithin(ReportInfo report) {
        return ReportUtils.isTimestampGreaterOrEqual(report.getReport(), since.getReport())
                && ReportUtils.isTimestampLessOrEqual(report.getReport(), till.getReport());
    }
}

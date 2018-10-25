package tracker;

import core.AltimeterMode;
import core.AltQNH;

import java.util.List;
import java.util.ArrayList;

import world.AirportAndDist;
import web.FLCalc;
import entities.Report;
import entities.ReportPilotPosition;
import forge.commons.Geo;

public class PilotData {
    private List<Report> reports = new ArrayList<Report>();
    private ReportPilotPosition[] reportPositions;

    private boolean nowOnline;
    private List<Position> positions = new ArrayList<Position>();
    private List<Segment> segments = new ArrayList<Segment>();
    private int pilotId;

    public PilotData(int pilotId) {
        this.pilotId = pilotId;
    }

    public void setReports(List<Report> reports) {
        this.reports.clear();
        this.reports.addAll(reports);
        this.reportPositions = new ReportPilotPosition[reports.size()];
    }

    public void setReportPosition(Report report, ReportPilotPosition position) {
        int index = reports.indexOf(report);
        if (index == -1) {
            throw new IllegalArgumentException();
        }

        reportPositions[index] = position;
    }

    public void build() {
        ReportPilotPosition prev = null;
        Position prevP = null;
        int prevI = -1;
        nowOnline = true;
        for (int i = 0; i < reports.size(); i++) {
            ReportPilotPosition curr = reportPositions[i];

            if (prev == null) { // first point or is nowIsOffline
                if (curr == null) {
                    // we have not found report yet
                    // pilot is nowIsOffline now
                    // continue
                    nowOnline = false;
                } else {
                    // we have found first report
                    // and go to next report
                    Position currP = new Position(curr);
                    positions.add(currP);

                    prev = curr;
                    prevI = i;
                    prevP = currP;
                }
            } else {
                if (curr == null) {
                    // we've located skipped report
                    // lets go to next report
                } else {
                    Position currP = new Position(curr);
                    positions.add(currP);

                    PilotData.Segment segment = addSegment(prevP, currP, null);

                    boolean sequenceOk = i == prevI + 1;
                    boolean bothOnGround = prevP.isOnGround() && currP.isOnGround();

                    if (bothOnGround) {
                        if (segment.getSpeed() >= 200) {
                            segment.type = SegmentType.JumpOnGround;
                        } else {
                            segment.type = SegmentType.Taxi;
                        }
                    } else {
                        if (sequenceOk) {
                            // we've got absolutely ok sequence of reports
                            // need to draw normal track segment
                            segment.type = SegmentType.Flying;
                        } else {
                            // we've got missing reports
                            segment.type = SegmentType.MissingReports;
                        }
                    }

                    prev = curr;
                    prevI = i;
                    prevP = currP;
                }
            }
        }

    }

    private Segment addSegment(Position prevP, Position currP, SegmentType segmentType) {
        Segment segment = new Segment();
        segment.type = segmentType;
        segment.p1 = prevP;
        segment.p2 = currP;
        prevP.next = segment;
        currP.prev = segment;
        segments.add(segment);
        return segment;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public int getPilotId() {
        return pilotId;
    }

    public Position getPositionByReportId(int reportId) {
        for (Position position : positions) {
            if (position.getPosition().getReportId() == reportId) {
                return position;
            }
        }
        return null;
    }

    public boolean isNowOnline() {
        return nowOnline;
    }

    public class Position {
        private ReportPilotPosition position;
        private Segment prev;
        private Segment next;
        private AirportAndDist nearestAirport;
        private AltimeterMode nearestAirportAltimeterPolicy;
        private int nearestAirportTA;
        private int nearestAirportTL;
        private int nearestAirportTLAltitude;

        private AltimeterMode altimeterMode;
        private int actualAltitude;
        private boolean onGround;

        public Position(ReportPilotPosition position) {
            this.position = position;

            nearestAirport = TrackerData.airports.findNearestLL9(position.getLatitude(), position.getLongitude());
            if (nearestAirport != null) {
                nearestAirportAltimeterPolicy = AltimeterMode.QNH;
                if (nearestAirport.getAirport().getIcao().startsWith("U")) {
                    nearestAirportAltimeterPolicy = AltimeterMode.QFE;
                }

                nearestAirportTA = 5000;

                // calculate TL
                nearestAirportTL = nearestAirportTA + 1000;
                while (true) {
                    AltQNH altQnh = AltQNH.get(nearestAirportTL, position.getQnhMb());
                    int d = altQnh.getAltitude() - nearestAirportTA;
                    if (d >= 1000) {
                        nearestAirportTLAltitude = altQnh.getAltitude();
                        break;
                    }
                    nearestAirportTL = nearestAirportTL + 1000;
                }

                if (position.getAltitude() < nearestAirportTLAltitude) {
                    altimeterMode = nearestAirportAltimeterPolicy;
                } else {
                    altimeterMode = AltimeterMode.STD;
                }

                // let's calculate actual altitude
                if (altimeterMode == AltimeterMode.QNH) {
                    actualAltitude = position.getAltitude();
                } else if (altimeterMode == AltimeterMode.QFE) {
                    actualAltitude = position.getAltitude();// + nearestAirport.getAirport().getAltitude();
                } else { // STD
                    AltQNH altQnh = AltQNH.get(position.getAltitude(), position.getQnhMb());
                    actualAltitude = altQnh.getAltitude();
                }

                if (actualAltitude < nearestAirport.getAirport().getAltitude() + 200) {
                    onGround = true;
                } else {
                    onGround = false;
                }
            } else {
                altimeterMode = AltimeterMode.STD;
                AltQNH altQnh = AltQNH.get(position.getAltitude(), position.getQnhMb());
                actualAltitude = altQnh.getAltitude();
                onGround = false;
            }
        }

        public ReportPilotPosition getPosition() {
            return position;
        }

        public AirportAndDist getNearestAirport() {
            return nearestAirport;
        }

        public AltimeterMode getNearestAirportAltimeterPolicy() {
            return nearestAirportAltimeterPolicy;
        }

        public int getNearestAirportTA() {
            return nearestAirportTA;
        }

        public int getNearestAirportTL() {
            return nearestAirportTL;
        }

        public int getNearestAirportTLAltitude() {
            return nearestAirportTLAltitude;
        }

        public AltimeterMode getAltimeterMode() {
            return altimeterMode;
        }

        public int getActualAltitude() {
            return actualAltitude;
        }

        public String getActualFL() {
            if (altimeterMode == AltimeterMode.STD) {
                return "FL" + FLCalc.altToFL(actualAltitude);
            } else {
                return "A" + FLCalc.altToFL(actualAltitude);
            }
        }

        public boolean isOnGround() {
            return onGround;
        }

        public String getAircraft() {
            String fpAircraft = position.getFpAircraft();
            if (fpAircraft == null)
                return "";
            fpAircraft = fpAircraft.trim();
            if (fpAircraft.length() >= 2) {
                if (Character.isLetter(fpAircraft.charAt(0)) && fpAircraft.charAt(1) == '/') {
                    fpAircraft = fpAircraft.substring(2);
                }
            }
            int i = fpAircraft.indexOf('/');
            if (i != -1) {
                fpAircraft = fpAircraft.substring(0, i);
            }
            return fpAircraft;
        }
    }

    public class Segment {
        private SegmentType type;
        private Position p1;
        private Position p2;

        public SegmentType getType() {
            return type;
        }

        public Position getP1() {
            return p1;
        }

        public Position getP2() {
            return p2;
        }

        public double getDistance() {
            return Geo.distanceNM(
                    p1.getPosition().getLatitude(), p1.getPosition().getLongitude(),
                    p2.getPosition().getLatitude(), p2.getPosition().getLongitude());
        }

        public int getDurationInSecs() {
            Report r1 = PilotData.this.getReportById(p1.getPosition().getReportId());
            Report r2 = PilotData.this.getReportById(p2.getPosition().getReportId());
            return (int) ((r1.getReportDt().getMillis() - r2.getReportDt().getMillis()) / 1000);
        }

        public double getSpeed() {
            double dist = getDistance();
            double time = getDurationInSecs();
            double speed = dist / (time / 3600.0);
            return speed;
        }
    }

    private Report getReportById(int reportId) {
        for (Report reportInfo : reports) {
            if (reportInfo.getId() == reportId) {
                return reportInfo;
            }
        }
        return null;
    }

    public enum SegmentType {
        Flying, MissingReports, JumpOnGround, Taxi
    }
}

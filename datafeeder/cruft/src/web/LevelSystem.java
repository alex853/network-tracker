package web;

import java.util.List;
import java.util.ArrayList;

public class LevelSystem {
    public static final LevelSystem RVSM = new LevelSystem();

    private List<FL> levels = new ArrayList<FL>();

    private LevelSystem() {
        levels.add(new FL( 60, Direction.West));
        levels.add(new FL( 70, Direction.East));
        levels.add(new FL( 80, Direction.West));
        levels.add(new FL( 90, Direction.East));
        levels.add(new FL(100, Direction.West));
        levels.add(new FL(110, Direction.East));
        levels.add(new FL(120, Direction.West));
        levels.add(new FL(130, Direction.East));
        levels.add(new FL(140, Direction.West));
        levels.add(new FL(150, Direction.East));
        levels.add(new FL(160, Direction.West));
        levels.add(new FL(170, Direction.East));
        levels.add(new FL(180, Direction.West));
        levels.add(new FL(190, Direction.East));
        levels.add(new FL(200, Direction.West));
        levels.add(new FL(210, Direction.East));
        levels.add(new FL(220, Direction.West));
        levels.add(new FL(230, Direction.East));
        levels.add(new FL(240, Direction.West));
        levels.add(new FL(250, Direction.East));
        levels.add(new FL(260, Direction.West));
        levels.add(new FL(270, Direction.East));
        levels.add(new FL(280, Direction.West));
        levels.add(new FL(290, Direction.East));
        levels.add(new FL(300, Direction.West));
        levels.add(new FL(310, Direction.East));
        levels.add(new FL(320, Direction.West));
        levels.add(new FL(330, Direction.East));
        levels.add(new FL(340, Direction.West));
        levels.add(new FL(350, Direction.East));
        levels.add(new FL(360, Direction.West));
        levels.add(new FL(370, Direction.East));
        levels.add(new FL(380, Direction.West));
        levels.add(new FL(390, Direction.East));
        levels.add(new FL(400, Direction.West));
        levels.add(new FL(410, Direction.East));
        levels.add(new FL(430, Direction.West));
        levels.add(new FL(450, Direction.East));
        levels.add(new FL(470, Direction.West));
        levels.add(new FL(490, Direction.East));
        levels.add(new FL(510, Direction.West));
        levels.add(new FL(530, Direction.East));
        levels.add(new FL(550, Direction.West));
        levels.add(new FL(570, Direction.East));
        levels.add(new FL(590, Direction.West));
        levels.add(new FL(610, Direction.East));
    }

    public FL getNearest(int altitude) {
        int minDeviation = Integer.MAX_VALUE;
        FL nearest = null;
        for (FL level : levels) {
            int deviation = Math.abs(level.getDeviation(altitude));
            if (deviation < minDeviation) {
                minDeviation = deviation;
                nearest = level;
            }
        }
        return nearest;
    }

    public FL getNearestForDirection(int altitude, int heading) {
        Direction direction = Direction.eastWestByHeading(heading);
        int minDeviation = Integer.MAX_VALUE;
        FL nearest = null;
        for (FL level : levels) {
            if (level.getDirection() != direction)
                continue;
            int deviation = Math.abs(level.getDeviation(altitude));
            if (deviation < minDeviation) {
                minDeviation = deviation;
                nearest = level;
            }
        }
        return nearest;
    }

    public String getName() {
        return "RVSM";
    }
}

package web;

public class FL {
    private int fl;
    private Direction direction;

    public FL(int fl, Direction direction) {
        this.fl = fl;
        this.direction = direction;
    }

    public String toString() {
        return FLCalc.printFL(fl);
    }

    public int getDeviation(int altitude) {
        return fl * 100 - altitude;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isOnLevel(int altitude) {
        return Math.abs(getDeviation(altitude)) < 200;
    }
}

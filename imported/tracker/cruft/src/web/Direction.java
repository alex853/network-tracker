package web;

public enum Direction {
    East,
    West;

    public static Direction eastWestByHeading(int heading) {
        if (heading >= 0 && heading <= 179)
            return East;
        if (heading >= 180 && heading <= 359)
            return West;
        throw new IllegalStateException("Illegal heading " + heading);
    }
}

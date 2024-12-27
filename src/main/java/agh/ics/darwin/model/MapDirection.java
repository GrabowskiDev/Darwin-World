package agh.ics.darwin.model;

public enum MapDirection {
    NORTH,            //0
    NORTHEAST,        //1
    EAST,             //2
    SOUTHEAST,        //3
    SOUTH,            //4
    SOUTHWEST,        //5
    WEST,             //6
    NORTHWEST;        //7

    public String toString() {
        return switch(this) {
            case NORTH -> "^";
            case NORTHEAST -> "↗";
            case EAST -> ">";
            case SOUTHEAST -> "↘";
            case SOUTH -> "v";
            case SOUTHWEST -> "↙";
            case WEST -> "<";
            case NORTHWEST -> "↖";
        };
    }

    public MapDirection next() {
        return switch (this) {
            case NORTH -> NORTHEAST;
            case NORTHEAST -> EAST;
            case EAST -> SOUTHEAST;
            case SOUTHEAST -> SOUTH;
            case SOUTH -> SOUTHWEST;
            case SOUTHWEST -> WEST;
            case WEST -> NORTHWEST;
            case NORTHWEST -> NORTH;
        };
    }

    public MapDirection rotate(int times) {
        if (times < 0 || times > 7) {
            throw new IllegalArgumentException("Invalid number of rotations (should be 0-7, got " + times + ")");
        }
        MapDirection result = this;
        for (int i = 0; i < times; i++) {
            result = result.next();
        }
        return result;
    }

    public Vector2d toUnitVector() {
        return switch (this) {
            case NORTH -> new Vector2d(0, 1);
            case NORTHEAST -> new Vector2d(1, 1);
            case EAST -> new Vector2d(1, 0);
            case SOUTHEAST -> new Vector2d(1, -1);
            case SOUTH -> new Vector2d(0, -1);
            case SOUTHWEST -> new Vector2d(-1, -1);
            case WEST -> new Vector2d(-1, 0);
            case NORTHWEST -> new Vector2d(-1, 1);
        };
    }
}

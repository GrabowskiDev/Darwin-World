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

    public void rotate(int n) {
        if (n>0) {
            for (int i = 0; i < n; i++) {
                this.next();
            }
        }
    }

    public MapDirection prev() {
        return switch (this) {
            case NORTH -> NORTHWEST;
            case NORTHEAST -> NORTH;
            case EAST -> NORTHEAST;
            case SOUTHEAST -> EAST;
            case SOUTH -> SOUTHEAST;
            case SOUTHWEST -> SOUTH;
            case WEST -> SOUTHWEST;
            case NORTHWEST -> WEST;
        };
    }
}

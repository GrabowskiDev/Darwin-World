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
            case NORTH -> "Północ";
            case NORTHEAST -> "Północny-Wschód";
            case EAST -> "Wschód";
            case SOUTHEAST -> "Południowy-Wschód";
            case SOUTH -> "Południe";
            case SOUTHWEST -> "Południowy-Zachód";
            case WEST -> "Zachód";
            case NORTHWEST -> "Północny-Zachód";
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

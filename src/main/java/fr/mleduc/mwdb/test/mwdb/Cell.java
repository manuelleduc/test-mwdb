package fr.mleduc.mwdb.test.mwdb;

/**
 * Created by mleduc on 17/03/16.
 */
public class Cell {
    private final long x;
    private final long y;

    public Cell(long x, long y) {
        this.x = x;
        this.y = y;
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }


    @Override
    public String toString() {
        return "Cell{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

package fr.mleduc.mwdb.test.mwdb;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mleduc on 10/03/16.
 */
public class CellGrid {

    public final boolean empty;
    public final Long minX;
    public final Long maxX;
    public final Long minY;
    public final Long maxY;
    private final List<Cell> cells = new ArrayList<>();

    public CellGrid() {
        this.empty = true;
        this.minX = null;
        this.maxX = null;
        this.minY = null;
        this.maxY = null;
    }

    private CellGrid(CellGrid cellGrid, Cell cell) {
        if(cellGrid.empty) {
            this.minX = cell.getX();
            this.maxX = cell.getX();
            this.minY = cell.getY();
            this.maxY = cell.getY();
        } else {
            this.minX = getMin(cellGrid.minX, cell.getX());
            this.maxX = getMax(cellGrid.maxX, cell.getX());
            this.minY = getMin(cellGrid.minY, cell.getY());
            this.maxY = getMax(cellGrid.maxY, cell.getY());
        }
        this.cells.addAll(cellGrid.cells);
        this.cells.add(cell);
        this.empty = false;
    }

    public CellGrid(CellGrid cg1, CellGrid cg2) {
        if(cg1.empty) {
            this.empty = cg2.empty;
            this.minX = cg2.minX;
            this.maxX = cg2.maxX;
            this.minY = cg2.minY;
            this.maxY = cg2.maxY;
            this.cells.addAll(cg2.cells);
        } else {
            this.empty = false;
            this.cells.addAll(cg1.cells);
            this.cells.addAll(cg2.cells);
            this.minX = getMin(cg1.minX, cg2.minX);
            this.maxX = getMin(cg1.maxX, cg2.maxX);
            this.minY = getMin(cg1.minY, cg2.minY);
            this.maxY = getMin(cg1.maxY, cg2.maxY);
        }
    }

    public CellGrid(List<Cell> lstCells) {
        if(lstCells == null || lstCells.size() == 0) {
            this.empty = true;
            this.minX = null;
            this.maxX = null;
            this.minY = null;
            this.maxY = null;
        } else {
            this.empty = false;
            this.cells.addAll(lstCells);
            this.minX = cells.stream().map(Cell::getX).min((o1, o2) -> o1.compareTo(o2)).orElse(0L);
            this.maxX = cells.stream().map(Cell::getX).max((o1, o2) -> o1.compareTo(o2)).orElse(0L);
            this.minY = cells.stream().map(Cell::getY).min((o1, o2) -> o1.compareTo(o2)).orElse(0L);
            this.maxY = cells.stream().map(Cell::getY).max((o1, o2) -> o1.compareTo(o2)).orElse(0L);
        }
    }


    private long getMax(long a, long b) {
        final Long max;
        if(a < b) {
            max = b;
        } else {
            max = a;
        }
        return max;
    }

    private long getMin(final long a, final long b) {
        final Long min;
        if(a > b) {
            min = b;
        } else {
            min = a;
        }
        return min;
    }

    public CellGrid add(Cell o) {
        return new CellGrid(this, o);
    }

    public boolean isAlive(long x, long y) {
        for(Cell c: cells) {
            if(c.getX() == x && c.getY() == y) {
                return true;
            }
        }
        return false;
    }

    public int countNeighbourAlive(long x, long y) {
        int cpt = 0;
        for(Cell c: cells) {
            final Long cellX = c.getX();
            final Long cellY = c.getY();
            final boolean aroundX = cellX.equals(x - 1L) || cellX.equals(x + 1L);
            final boolean aroundY = cellY.equals(y - 1L) || cellY.equals(y + 1L);
            final boolean eqY = cellY.equals(y);
            final boolean eqX = cellX.equals(x);
            if(aroundX && aroundY || eqY && aroundX || eqX && aroundY) {
                cpt++;
            }
        }
        return cpt;
    }

    public Cell getByCoordinates(long x, long y) {
        for(Cell c: cells) {
            if(c.getX() == x && c.getY() == y) {
                return c;
            }
        }
        return null;
    }

    public CellGrid add(CellGrid cellGrid2) {
        return new CellGrid(this, cellGrid2);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if(!this.empty) {
            for (long y = this.minY; y <= this.maxY; y++) {
                for (long x = this.minX; x <= this.maxX; x++) {
                    if (this.isAlive(x, y)) {
                        sb.append('x');
                    } else {
                        sb.append(' ');
                    }
                }
                sb.append('\n');
            }
        } else {
            sb.append('*');
        }
        return sb.toString();
    }
}

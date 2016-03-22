package fr.mleduc.mwdb.test.mwdb;

import java.util.*;

/**
 * Created by mleduc on 10/03/16.
 */
public class CellGrid {

    private final Set<Cell> cells = new TreeSet<>();
    public final Set<Cell> nextTime = new TreeSet<>();

    public CellGrid() {

    }

    private CellGrid(CellGrid cellGrid, Cell cell) {
        this.cells.addAll(cellGrid.cells);
        this.nextTime.addAll(cellGrid.nextTime);
        this.cells.add(cell);
        addAllAndMe(cell);
    }

    private void addAllAndMe(Cell cell) {
        this.nextTime.addAll(cell.getNeighbours());
        this.nextTime.add(cell);
    }

    public CellGrid(CellGrid cg1, CellGrid cg2) {
        this.cells.addAll(cg1.cells);
        this.nextTime.addAll(cg1.nextTime);
        this.cells.addAll(cg2.cells);
        this.nextTime.addAll(cg2.nextTime);
    }

    public CellGrid(List<Cell> lstCells) {
        lstCells.stream().forEach(cell -> {
            this.cells.add(cell);
            addAllAndMe(cell);
        });
    }


    public CellGrid add(Cell o) {
        return new CellGrid(this, o);
    }

    public boolean isAlive(long x, long y) {
        return this.cells.contains(new Cell(x,y));
    }

    public long countNeighbourAlive(final long x, final long y) {
        final Collection<? extends Cell> neighbours = new Cell(x, y).getNeighbours();
        return this.cells.stream().filter(neighbours::contains).count();
    }

    //public CellGrid add(CellGrid cellGrid2) {
//        return new CellGrid(this, cellGrid2);
  //  }

   /* @Override
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
    }*/
}

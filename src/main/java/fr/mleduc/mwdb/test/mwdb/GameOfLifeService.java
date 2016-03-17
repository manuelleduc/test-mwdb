package fr.mleduc.mwdb.test.mwdb;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mleduc on 11/03/16.
 */
public class GameOfLifeService {
    public List<LifeOperation> doLife(final CellGrid cellGrid) {
        final List<LifeOperation> ret = new ArrayList<>();
        if(!cellGrid.empty) {
            for (long x = cellGrid.minX-1; x <= cellGrid.maxX+1; x++) {
                for (long y = cellGrid.minY-1; y <= cellGrid.maxY+1; y++) {
                    if (cellGrid.isAlive(x, y)) {
                        int cpt = cellGrid.countNeighbourAlive(x, y);
                        if (cpt < 2 || cpt > 3) {
                            final Cell cell = cellGrid.getByCoordinates(x, y);
                            ret.add(LifeOperation.deadCell(cell.getX(), cell.getY()));
                        }
                    } else {
                        int cpt = cellGrid.countNeighbourAlive(x, y);
                        if (cpt == 3) {
                            ret.add(LifeOperation.newCell(x,y));
                        }
                    }
                }
            }
        }
        return  ret;
    }
}

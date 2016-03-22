package fr.mleduc.mwdb.test.mwdb;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;

/**
 * Created by mleduc on 11/03/16.
 */
public class GameOfLifeServiceTest {

    private final GameOfLifeService gameOfLifeService = new GameOfLifeService();

    @Test
    public void rule1() throws Exception {


        final Cell cell1 = new Cell(0L, 0L);

        final CellGrid cellGrid = new CellGrid().add(cell1);
        final List<LifeOperation> res = gameOfLifeService.doLife(cellGrid);
        Assertions.assertThat(res)
                .contains(LifeOperation.deadCell(0, 0))
                .hasSize(1);
    }

    @Test
    public void rule2() throws Exception {

        final Cell cell1 = new Cell(0L, 0L);
        final Cell cell2 = new Cell(0L, 1L);
        final Cell cell3 = new Cell(1L, 0L);

        final CellGrid cellGrid = new CellGrid().add(cell1).add(cell2).add(cell3);
        final List<LifeOperation> res = gameOfLifeService.doLife(cellGrid);
        Assertions.assertThat(res)
                .contains(LifeOperation.newCell(1, 1))
                .hasSize(1);
    }

    @Test
    public void rule3() throws Exception {

        final Cell cell1 = new Cell(0L, 0L);
        final Cell cell2 = new Cell(0L, 1L);
        final Cell cell3 = new Cell(1L, 0L);
        final Cell cell4 = new Cell(0L, -1L);
        final Cell cell5 = new Cell(-1L, -1L);

        final CellGrid cellGrid = new CellGrid().add(cell1).add(cell2).add(cell3).add(cell4).add(cell5);
        final List<LifeOperation> res = gameOfLifeService.doLife(cellGrid);
        Assertions.assertThat(res)
                .contains(LifeOperation.deadCell(0, 0),
                        LifeOperation.newCell(1, 1),
                        LifeOperation.newCell(1, -1)).hasSize(3);

    }

    @Test
    public void rule4() throws Exception {

        final Cell cell1 = new Cell(0L, 0L);
        final Cell cell2 = new Cell(0L, 2L);
        final Cell cell3 = new Cell(2L, 2L);

        final CellGrid cellGrid = new CellGrid().add(cell1).add(cell2).add(cell3);
        final List<LifeOperation> res = gameOfLifeService.doLife(cellGrid);
        Assertions.assertThat(res)
                .contains(LifeOperation.deadCell(0, 0), LifeOperation.deadCell(0, 2), LifeOperation.deadCell(2, 2), LifeOperation.newCell(1, 1))
                .hasSize(4);

    }

    /**
     * Start :
     * ###
     * # #
     * ###
     * <p>
     * Res :
     * #
     * # #
     * #   #
     * # #
     * #
     *
     * @throws Exception
     */
    @Test
    public void testSquareBehavioud() throws Exception {

        final Cell cell1 = new Cell(0L, 0L);
        final Cell cell2 = new Cell(0L, 1L);
        final Cell cell3 = new Cell(0L, 2L);
        final Cell cell4 = new Cell(1L, 0L);
        final Cell cell6 = new Cell(1L, 2L);
        final Cell cell7 = new Cell(2L, 0L);
        final Cell cell8 = new Cell(2L, 1L);
        final Cell cell9 = new Cell(2L, 2L);

        final CellGrid cellGrid = new CellGrid()
                .add(cell1)
                .add(cell2)
                .add(cell3)
                .add(cell4)
                .add(cell6)
                .add(cell7)
                .add(cell8)
                .add(cell9);
        final List<LifeOperation> res = gameOfLifeService.doLife(cellGrid);
        Assertions.assertThat(res)
                .contains(LifeOperation.newCell(1, 3), LifeOperation.deadCell(1, 2), LifeOperation.newCell(-1, 1),
                        LifeOperation.deadCell(0, 1), LifeOperation.deadCell(2, 1), LifeOperation.newCell(3, 1),
                        LifeOperation.deadCell(1, 0), LifeOperation.newCell(1, -1))
                .hasSize(8);
    }

    @Test
    public void testSquareBehaviour2() throws Exception {

        final Cell cell1 = new Cell(0L, 0L);
        final Cell cell2 = new Cell(1L, -1L);
        final Cell cell3 = new Cell(2L, 0L);
        final Cell cell4 = new Cell(3L, 1L);
        final Cell cell6 = new Cell(2L, 2L);
        final Cell cell7 = new Cell(1L, 3L);
        final Cell cell8 = new Cell(0L, 2L);
        final Cell cell9 = new Cell(-1L, 1L);

        final CellGrid cellGrid = new CellGrid()
                .add(cell1)
                .add(cell2)
                .add(cell3)
                .add(cell4)
                .add(cell6)
                .add(cell7)
                .add(cell8)
                .add(cell9);
        final List<LifeOperation> res = gameOfLifeService.doLife(cellGrid);
        Assertions.assertThat(res)
                .contains(LifeOperation.newCell(0, 1),
                        LifeOperation.newCell(1, 0),
                        LifeOperation.newCell(1, 2),
                        LifeOperation.newCell(2, 1))
                .hasSize(4);
    }
}

package fr.mleduc.mwdb.test.mwdb.task.action;

import fr.mleduc.mwdb.test.mwdb.Cell;
import fr.mleduc.mwdb.test.mwdb.CellGrid;
import org.mwg.Node;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mleduc on 07/05/16.
 */
public class CastAndAggregate implements TaskAction {
    @Override
    public void eval(TaskContext context) {
        final List<Node> kNodes = Arrays.asList((Node[]) context.getPreviousResult());

        final List<Cell> lstCells = kNodes.stream().filter(kNode -> kNode != null)
                .map(node -> {
                    final long x = (long) node.get("x");
                    final long y = (long) node.get("y");
                    final Cell cell = new Cell(x, y);
                    node.free();
                    return cell;
                }).collect(Collectors.toList());
        final CellGrid actionResult = new CellGrid(lstCells);
        context.setResult(actionResult);
        context.next();
    }
}
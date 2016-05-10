package fr.mleduc.mwdb.test.mwdb.task.action;

import fr.mleduc.mwdb.test.mwdb.CellGrid;
import fr.mleduc.mwdb.test.mwdb.GameOfLifeService;
import fr.mleduc.mwdb.test.mwdb.LifeOperation;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

import java.util.List;

/**
 * Created by mleduc on 07/05/16.
 */
public class DoLifeTaskAction implements TaskAction {
    @Override
    public void eval(final TaskContext context) {
        final List<LifeOperation> res = new GameOfLifeService().doLife((CellGrid) context.getPreviousResult());
        context.setResult(res);
        context.next();
    }
}

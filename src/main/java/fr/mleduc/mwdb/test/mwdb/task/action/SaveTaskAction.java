package fr.mleduc.mwdb.test.mwdb.task.action;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

/**
 * Created by mleduc on 04/05/16.
 */
public class SaveTaskAction implements TaskAction {
    @Override
    public void eval(final TaskContext context) {
        context.graph().save(result -> {
            context.setResult(result);
            context.next();
        });
    }
}

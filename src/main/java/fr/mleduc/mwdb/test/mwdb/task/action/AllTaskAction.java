package fr.mleduc.mwdb.test.mwdb.task.action;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

/**
 * Created by mleduc on 07/05/16.
 */
public class AllTaskAction implements TaskAction {
    private final String indexName;

    public AllTaskAction(final String indexName) {
        this.indexName = indexName;
    }

    @Override
    public void eval(final TaskContext context) {
        context.graph().all(context.getWorld(), context.getTime(), indexName, result -> {
            context.setResult(result);
            context.next();
        });
    }
}
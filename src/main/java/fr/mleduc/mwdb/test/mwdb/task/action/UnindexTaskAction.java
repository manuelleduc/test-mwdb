package fr.mleduc.mwdb.test.mwdb.task.action;

import org.mwg.Node;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

/**
 * Created by mleduc on 04/05/16.
 */
public class UnindexTaskAction implements TaskAction {
    private final String indexName;
    private final String[] attributes;

    public UnindexTaskAction(final String indexName, final String[] attributes) {
        this.indexName = indexName;
        this.attributes = attributes;
    }

    @Override
    public void eval(final TaskContext context) {
        final Node cell = (Node) context.getPreviousResult();
        context.graph().unindex(indexName, cell, attributes, result -> {
            cell.free();
            context.setResult(result);
            context.next();
        });
    }
}

package fr.mleduc.mwdb.test.mwdb.task.action;

import org.mwg.Node;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

/**
 * Created by mleduc on 04/05/16.
 */
public class IndexTaskAction implements TaskAction {
    private final String indexName;
    private final Node node;
    private final String[] keyAttributes;

    public IndexTaskAction(final String indexName, final Node node, final String[] keyAttributes) {
        this.indexName = indexName;
        this.node = node;
        this.keyAttributes = keyAttributes;
    }

    @Override
    public void eval(final TaskContext context) {
        context.graph().index(indexName, node, keyAttributes, result -> {
            node.free();
            context.setResult(result);
            context.next();
        });
    }
}

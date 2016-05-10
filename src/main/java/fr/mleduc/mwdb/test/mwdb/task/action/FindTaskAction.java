package fr.mleduc.mwdb.test.mwdb.task.action;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

/**
 * Created by mleduc on 04/05/16.
 */
public class FindTaskAction implements TaskAction {
    private final long time;
    private final String indexName;
    private final String query;
    private final long world;

    public FindTaskAction(final long world, final long time, final String indexName, final String query) {
        this.world = world;
        this.time = time;
        this.indexName = indexName;
        this.query = query;
    }

    @Override
    public void eval(final TaskContext context) {
        context.graph().find(world, time, indexName, query, (cells) -> {
            context.setResult(cells[0]);
            context.next();
        });

    }
}
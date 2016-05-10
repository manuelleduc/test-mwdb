package fr.mleduc.mwdb.test.mwdb;

import etm.core.configuration.BasicEtmConfigurator;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;
import etm.core.renderer.SimpleTextRenderer;
import fr.mleduc.mwdb.test.mwdb.task.action.*;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.core.NoopScheduler;
import org.mwg.task.Task;

import java.util.ArrayList;

/**
 * Created by mleduc on 04/05/16.
 */
public class ApplicationTask {
    public static void main(String[] args) {
        BasicEtmConfigurator.configure();
        final EtmMonitor monitor = EtmManager.getEtmMonitor();
        monitor.start();

        final int max = 10000;
        final int itts = 1;

        for (int i = 0; i < itts; i++) {
            wholeProcess(monitor, max, i);
        }

        monitor.stop();
        monitor.render(new SimpleTextRenderer());
    }

    private static void wholeProcess(EtmMonitor monitor, int max, int iterationLoop) {
        EtmPoint point = monitor.createPoint("start");
        final Graph graph = GraphBuilder.builder()
                .withScheduler(new NoopScheduler())
                .withOffHeapMemory()
                .build();
        graph.connect(connectResult -> {

            final ArrayList<Object> lifeOperations = new ArrayList<>();

            lifeOperations.add(LifeOperation.newCell(0, 0));
            lifeOperations.add(LifeOperation.newCell(0, 1));
            lifeOperations.add(LifeOperation.newCell(0, 2));
            lifeOperations.add(LifeOperation.newCell(1, 0));
            lifeOperations.add(LifeOperation.newCell(1, 2));
            lifeOperations.add(LifeOperation.newCell(2, 0));
            lifeOperations.add(LifeOperation.newCell(2, 1));
            lifeOperations.add(LifeOperation.newCell(2, 2));
            graph
                    .newTask()
                    .from(lifeOperations.toArray())
                    .foreachPar(graph.newTask().then(context -> {
                        proceedLifeOperations((LifeOperation) context.getPreviousResult(), context.graph(), context.getTime());
                    }))
                    .then(new SaveTaskAction())
                    .from(generateNumbers(1, max))
                    .foreach(graph.newTask().then(context -> {
                        Long time = (Long) context.getPreviousResult();
                        EtmPoint point2 = monitor.createPoint("loop");
                        step(graph, time);
                        point2.collect();
                    }))
                    .time(max)
                    .then(new AllTaskAction("cells"))
                    .then(new CastAndAggregate())
                    .then(context -> System.out.println(context.getPreviousResult()))
                    .then(context -> graph.disconnect(disconnectResult -> point.collect()))
                    .execute();
        });
    }

    private static void step(final Graph graph, final Long time) {
        final String indexName = "cells";
        graph
                .newTask()
                .time(time)
                .then(new AllTaskAction(indexName))
                .then(new CastAndAggregate())
                .then(new DoLifeTaskAction())
                .foreach(graph.newTask().then(context -> proceedLifeOperations((LifeOperation) context.getPreviousResult(), context.graph(), context.getTime())))
                .then(new SaveTaskAction())
                .execute();
    }

    private static ArrayList<Long> generateNumbers(int i, int max) {
        final ArrayList<Long> integers = new ArrayList<>();
        for (int x = i; x <= max; x++) {
            integers.add((long) x);
        }
        return integers;
    }

    private static void proceedLifeOperations(LifeOperation lifeOperation, Graph graph, long time) {
        Task t;
        if (lifeOperation.type == LifeOperation.LifeOperationType.New) {
            final Node cell = createCell(graph, time, lifeOperation.x, lifeOperation.y);
            t = indexCell(graph, cell);
        } else {
            t = removeCell(graph, time, lifeOperation.x, lifeOperation.y);
        }
        t.execute();
    }


    private static Task removeCell(Graph graph, long time, long x, long y) {
        return graph.newTask()
                .then(new FindTaskAction(0, time, "cells", "x=" + x + ",y=" + y))
                .then(new UnindexTaskAction("cells", new String[]{"x", "y"}));
    }

    private static Task indexCell(Graph graph, Node cell) {
        return graph.newTask().then(new IndexTaskAction("cells", cell, new String[]{"x", "y"})).then(context -> {
            cell.free();
        });
    }

    private static Node createCell(final Graph graph, long time, long x, long y) {
        final Node cell = graph.newNode(0, time);
        cell.setProperty("x", Type.LONG, x);
        cell.setProperty("y", Type.LONG, y);
        return cell;
    }
}

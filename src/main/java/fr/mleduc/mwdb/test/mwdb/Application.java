package fr.mleduc.mwdb.test.mwdb;


import etm.core.configuration.BasicEtmConfigurator;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;
import etm.core.renderer.SimpleTextRenderer;
import org.jdeferred.Deferred;
import org.jdeferred.DeferredManager;
import org.jdeferred.DonePipe;
import org.jdeferred.Promise;
import org.jdeferred.impl.DefaultDeferredManager;
import org.jdeferred.impl.DeferredObject;
import org.jdeferred.multiple.MasterProgress;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.core.NoopScheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mleduc on 17/03/16.
 */
public class Application {
    public static void main(String[] args) throws InterruptedException {

        //Thread.sleep(30000);
        //Thread.sleep(3000);
        // 1 -> init a first list of LifeOperation (life only)
        // 2 -> persist it
        // 3 -> retrieve a cell grid <--------------+
        // 4 -> produce a serie of life actions     |
        // 5 -> persit it                           |
        // 6 -> repeat N time ----------------------+

        BasicEtmConfigurator.configure();
        final EtmMonitor monitor = EtmManager.getEtmMonitor();
        monitor.start();

        final long dim1 = 20;
        final long dim2 = 20;
        final int max = 10_000;
        final int itts = 1;
        for (int i = 0; i < itts; i++) {
            wholeProcess(monitor, dim1, dim2, max, i);
        }

        monitor.stop();
        monitor.render(new SimpleTextRenderer());


    }

    private static void wholeProcess(EtmMonitor monitor, long dim1, long dim2, int max, int iterationLoop) {
        System.out.println("Start " + iterationLoop);
        EtmPoint point = monitor.createPoint("start");

        final long initialCapacity = (long) (dim1 * dim2 * 1.1);
        final int l = (int) (dim1 * dim2 * 1.1);
        final Graph graph = GraphBuilder.builder()
                .withScheduler(new NoopScheduler())
                .withOffHeapMemory().build();

        final Deferred<Boolean, Object, Object> connectDeferred = connect(graph);

        connectDeferred.then(o -> {
            final List<LifeOperation> lifeOperations = new ArrayList<>();
            /*Random r = new Random();
            for(int i = 0; i< dim1; i++) {
                for(int j = 0; j< dim2; j++) {
                    if(r.nextInt() % 100 > 75) {
                        lifeOperations.add(LifeOperation.newCell(i,j));
                    }
                }
            }*/

            lifeOperations.add(LifeOperation.newCell(0, 0));
            lifeOperations.add(LifeOperation.newCell(0, 1));
            lifeOperations.add(LifeOperation.newCell(0, 2));
            lifeOperations.add(LifeOperation.newCell(1, 0));
            lifeOperations.add(LifeOperation.newCell(1, 2));
            lifeOperations.add(LifeOperation.newCell(2, 0));
            lifeOperations.add(LifeOperation.newCell(2, 1));
            lifeOperations.add(LifeOperation.newCell(2, 2));

            Promise<Boolean, Object, Object> firstLifeOperations = proceedLifeOperations(graph, 0, lifeOperations)
                    .then((MultipleResults result) -> save(graph));
            //final List<int> jksdfjksdf = new Arr
            for (int i = 1; i < max; i++) {
                //long start = System.currentTimeMillis();
                EtmPoint point2 = monitor.createPoint("loop");
                firstLifeOperations = step(graph, firstLifeOperations, i);
                point2.collect();

            }

            final Promise<CellGrid, Object, Object> then1 = firstLifeOperations
                    .then((Boolean result) -> getAllCells(graph, max));

            then1.then((CellGrid aaa) -> {
                point.collect();
                return aaa;
            }).done(c -> showState(max, c));
        });
        System.out.println("Stop " + iterationLoop);
    }

    private static Promise<Boolean, Object, Object> step(Graph graph, Promise<Boolean, Object, Object> promise, long lifeI) {
        return promise
                .then((Boolean result) -> getAllCells(graph, lifeI))
                //.then(c -> {showState(lifeI, c); })
                .then((CellGrid result) -> doLife(result))
                .then((List<LifeOperation> result) -> proceedLifeOperations(graph, lifeI, result))
                .then((MultipleResults result) -> save(graph))
                ;
    }

    private static void showState(long lifeI, CellGrid c) {
        System.out.println("State at " + lifeI);
        System.out.println(c);
    }

    private static Promise<List<LifeOperation>, Object, Object> doLife(final CellGrid result) {
        final DeferredObject<List<LifeOperation>, Object, Object> deferred = new DeferredObject<>();
        final List<LifeOperation> lifeOperations = new GameOfLifeService().doLife(result);
        deferred.resolve(lifeOperations);
        return deferred;
    }

    private static Deferred<Node[], Object, Object> getAllNodes(final Graph graph, final long time) {
        final Deferred<Node[], Object, Object> ret = new DeferredObject<>();
        graph.all(0, time, "cells", ret::resolve);
        return ret;
    }

    private static Promise<CellGrid, Object, Object> getAllCells(final Graph graph, final long time) {
        final DeferredObject<CellGrid, Object, Object> ret = new DeferredObject<>();
        getAllNodes(graph, time).then(result -> {
            final List<Cell> lstCells = Arrays.asList(result).stream()
                    .map(kNode -> {
                        final long x = (long) kNode.get("x");
                        final long y = (long) kNode.get("y");
                        final Cell cell = new Cell(x, y);
                        kNode.free();
                        return cell;
                    })
                    .collect(Collectors.toList());
            ret.resolve(new CellGrid(lstCells));
        });
        return ret;
    }

    private static Promise<MultipleResults, OneReject, MasterProgress> proceedLifeOperations(final Graph graph, final long time, final List<LifeOperation> lifeOperations) {
        final Deferred[] res = new Deferred[lifeOperations.size()];
        lifeOperations.stream().map(lifeOperation -> {
            final Promise<Boolean, Object, Object> ret;
            if (lifeOperation.type == LifeOperation.LifeOperationType.New) {
                // Life
                final Node cell = createCell(graph, time, lifeOperation.x, lifeOperation.y);
                ret = indexCell(graph, cell).then(result -> {
                    cell.free();
                });
            } else {
                // Death
                ret = removeCell(graph, time, lifeOperation.x, lifeOperation.y);
            }
            return ret;
        }).collect(Collectors.toList()).toArray(res);

        final DeferredManager barrierIndexes = new DefaultDeferredManager();
        return barrierIndexes.when(res);
    }

    private static Promise<Boolean, Object, Object> removeCell(final Graph graph, final long saveTime, final long x, final long y) {
        final Promise<Boolean, Object, Object> res = lookupCellByCoordinates(graph, saveTime, x, y)
                .then((DonePipe<Node, Boolean, Object, Object>) cell -> {
                    final Deferred<Boolean, Object, Object> ret = new DeferredObject<>();
                    if (cell == null) {
                        System.out.println("Cell(" + x + ", " + y + ") not found");
                    } else {
                        graph.unindex("cells", cell, new String[]{"x", "y"}, ret::resolve);
                    }
                    return ret.then((DonePipe<Boolean, Boolean, Object, Object>) result -> {
                        final DeferredObject<Boolean, Object, Object> resss = new DeferredObject<>();
                        cell.free();
                        resss.resolve(result);
                        return resss.promise();
                    });
                });
        return res;
    }

    private static Deferred<Node, Object, Object> lookupCellByCoordinates(final Graph graph, final long saveTime, final long x, final long y) {
        final Deferred<Node, Object, Object> deferred = new DeferredObject<>();
        final String query = "x=" + x + ",y=" + y;
        graph.find(0, saveTime, "cells", query, (cell) -> {
            deferred.resolve(cell[0]);
        });
        return deferred;
    }

    private static Node createCell(final Graph graph, long time, long x, long y) {
        final Node cell = graph.newNode(0, time);
        cell.setProperty("x", Type.LONG, x);
        cell.setProperty("y", Type.LONG, y);
        return cell;
    }

    private static Promise<Boolean, Object, Object> indexCell(final Graph graph, Node cell) {
        final Deferred<Boolean, Object, Object> ret = new DeferredObject<>();
        graph.index("cells", cell, new String[]{"x", "y"}, ret::resolve);
        return ret;
    }

    private static Deferred<Boolean, Object, Object> save(final Graph graph) {
        final Deferred<Boolean, Object, Object> ret = new DeferredObject<>();
        graph.save(ret::resolve);
        return ret;
    }

    private static Deferred<Boolean, Object, Object> connect(final Graph graph) {
        final Deferred<Boolean, Object, Object> ret = new DeferredObject<>();
        graph.connect(ret::resolve);
        return ret;
    }
}

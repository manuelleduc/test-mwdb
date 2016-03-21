package fr.mleduc.mwdb.test.mwdb;


import org.jdeferred.*;
import org.jdeferred.impl.DefaultDeferredManager;
import org.jdeferred.impl.DeferredObject;
import org.jdeferred.multiple.MasterProgress;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;
import org.mwdb.*;
import org.mwdb.chunk.heap.HeapChunkSpace;
import org.mwdb.chunk.offheap.OffHeapChunkSpace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mleduc on 17/03/16.
 */
public class Application {
    public static void main(String[] args) throws InterruptedException {

        Thread.sleep(30000);
        //Thread.sleep(3000);
        // 1 -> init a first list of LifeOperation (life only)
        // 2 -> persist it
        // 3 -> retrieve a cell grid <--------------+
        // 4 -> produce a serie of life actions     |
        // 5 -> persit it                           |
        // 6 -> repeat N time ----------------------+

        final KGraph graph = GraphBuilder.builder()
                //.withScheduler(new NoopScheduler())
                //.withSpace(new HeapChunkSpace(10000, 10000))
                .buildGraph();
        final Deferred<Boolean, Object, Object> connectDeferred = connect(graph);

        connectDeferred.then(o -> {
            final List<LifeOperation> lifeOperations = new ArrayList<>();
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
            final int max = 500;
            for (int i = 1; i < max; i++) {
                firstLifeOperations = step(graph, firstLifeOperations, i);
            }

            final Promise<CellGrid, Object, Object> then1 = firstLifeOperations
                    .then((Boolean result) -> getAllCells(graph, max));
            then1.done(c -> showState(max, c));
        });

    }

    private static Promise<Boolean, Object, Object> step(KGraph graph, Promise<Boolean, Object, Object> promise, long lifeI) {
        return promise
                .then((Boolean result) -> getAllCells(graph, lifeI))
                //.then(c -> {showState(lifeI, c); })
                .then((CellGrid result) -> doLife(result))
                .then((List<LifeOperation> result) -> proceedLifeOperations(graph, lifeI, result))
                .then((MultipleResults result) -> save(graph));
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

    private static Deferred<KNode[], Object, Object> getAllNodes(final KGraph graph, final long time) {
        final Deferred<KNode[], Object, Object> ret = new DeferredObject<>();
        graph.all(0, time, "cells", ret::resolve);
        return ret;
    }

    private static Promise<CellGrid, Object, Object> getAllCells(final KGraph graph, final long time) {
        final DeferredObject<CellGrid, Object, Object> ret = new DeferredObject<>();
        getAllNodes(graph, time).then(result -> {
            final List<Cell> lstCells = Arrays.asList(result).stream()
                    .map(kNode -> {
                        final long x = (long) kNode.att("x");
                        final long y = (long) kNode.att("y");
                        final Cell cell = new Cell(x, y);
                        return cell;
                    })
                    .collect(Collectors.toList());
            ret.resolve(new CellGrid(lstCells));
        });
        return ret;
    }

    private static Promise<MultipleResults, OneReject, MasterProgress> proceedLifeOperations(final KGraph graph, final long time, final List<LifeOperation> lifeOperations) {
        final Deferred[] res = new Deferred[lifeOperations.size()];
        lifeOperations.stream().map(lifeOperation -> {
            final Promise<Boolean, Object, Object> ret;
            if (lifeOperation.type == LifeOperation.LifeOperationType.New) {
                // Life
                final KNode cell = createCell(graph, time, lifeOperation.x, lifeOperation.y);
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

    private static Promise<Boolean, Object, Object> removeCell(final KGraph graph, final long saveTime, final long x, final long y) {
        final Promise<Boolean, Object, Object> res = lookupCellByCoordinates(graph, saveTime, x, y)
                .then((DonePipe<KNode, Boolean, Object, Object>) cell -> {
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

    private static Deferred<KNode, Object, Object> lookupCellByCoordinates(final KGraph graph, final long saveTime, final long x, final long y) {
        final Deferred<KNode, Object, Object> deferred = new DeferredObject<>();
        final String query = "x=" + x + ",y=" + y;
        graph.find(0, saveTime, "cells", query, (cell) -> {
            deferred.resolve(cell[0]);
        });
        return deferred;
    }

    private static KNode createCell(final KGraph graph, long time, long x, long y) {
        final KNode cell = graph.newNode(0, time);
        cell.attSet("x", KType.LONG, x);
        cell.attSet("y", KType.LONG, y);
        return cell;
    }

    private static Promise<Boolean, Object, Object> indexCell(final KGraph graph, KNode cell) {
        final Deferred<Boolean, Object, Object> ret = new DeferredObject<>();
        graph.index("cells", cell, new String[]{"x", "y"}, ret::resolve);
        return ret;
    }

    private static Deferred<Boolean, Object, Object> save(final KGraph graph) {
        final Deferred<Boolean, Object, Object> ret = new DeferredObject<>();
        graph.save(ret::resolve);
        return ret;
    }

    private static Deferred<Boolean, Object, Object> connect(final KGraph graph) {
        final Deferred<Boolean, Object, Object> ret = new DeferredObject<>();
        graph.connect(ret::resolve);
        return ret;
    }
}

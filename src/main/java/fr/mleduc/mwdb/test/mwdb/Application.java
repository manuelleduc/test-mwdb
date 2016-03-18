package fr.mleduc.mwdb.test.mwdb;


import org.jdeferred.*;
import org.jdeferred.impl.DefaultDeferredManager;
import org.jdeferred.impl.DeferredObject;
import org.jdeferred.multiple.MasterProgress;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;
import org.mwdb.*;
import org.mwdb.task.NoopScheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mleduc on 17/03/16.
 */
public class Application {
    public static void main(String[] args) {
        // 1 -> init a first list of LifeOperation (life only)
        // 2 -> persist it
        // 3 -> retrieve a cell grid <--------------+
        // 4 -> produce a serie of life actions     |
        // 5 -> persit it                           |
        // 6 -> repeat N time ----------------------+

        final KGraph graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).buildGraph();
        final Deferred<Boolean, Object, Object> connectDeferred = connect(graph);

        connectDeferred.then(o -> {
            final List<LifeOperation> lifeOperations = new ArrayList<>();
            lifeOperations.add(LifeOperation.newCell(0,0));
            lifeOperations.add(LifeOperation.newCell(0,1));
            lifeOperations.add(LifeOperation.newCell(0,2));
            lifeOperations.add(LifeOperation.newCell(1,0));
            lifeOperations.add(LifeOperation.newCell(1,2));
            lifeOperations.add(LifeOperation.newCell(2,0));
            lifeOperations.add(LifeOperation.newCell(2,1));
            lifeOperations.add(LifeOperation.newCell(2,2));

            final Promise<Boolean, Object, Object> then = proceedLifeOperations(graph, 0, lifeOperations)
                    .then((MultipleResults result) -> save(graph));
            long lifeI = 1L;
            final Promise<Boolean, Object, Object> then2 = then
                    .then((Boolean result) -> getAll(graph, lifeI))
                    .then((CellGrid result) -> doLife(result))
                    .then((List<LifeOperation> result) -> proceedLifeOperations(graph, lifeI, result))
                    .then((MultipleResults result) -> save(graph));
            final Promise<CellGrid, Object, Object> then1 = then2
                    .then((Boolean result) -> getAll(graph, lifeI+1));
            then1
                    .done(System.out::println);

        });

    }

    private static Promise<List<LifeOperation>, Object, Object> doLife(CellGrid result) {
        final DeferredObject<List<LifeOperation>, Object, Object> deferred = new DeferredObject<>();
        deferred.resolve(new GameOfLifeService().doLife(result));
        return deferred;
    }

    private static Promise<CellGrid, Object, Object> getAll(KGraph graph, long time) {
        final DeferredObject<CellGrid, Object, Object> deferred = new DeferredObject<>();
        getAllCells(graph, time).then(result2 -> {
            final List<Cell> lstCells = Arrays.asList(result2).stream()
                    .map(kNode -> {
                        final long x = (long) kNode.att("x");
                        final long y = (long) kNode.att("y");
                        System.out.println("Cell("+x+","+y+") = alive ? " + kNode.att("a"));
                        return new Cell(x, y);
                    })
                    .collect(Collectors.toList());
            deferred.resolve(new CellGrid(lstCells));
        });
        return deferred;
    }

    private static Promise<MultipleResults, OneReject, MasterProgress> proceedLifeOperations(final KGraph graph, final long saveTime, final List<LifeOperation> lifeOperations) {
        final Deferred[] res = new Deferred[lifeOperations.size()];
        lifeOperations.stream().map(lifeOperation -> {
            final Promise<Boolean, Object, Object> ret;
            if(lifeOperation.type == LifeOperation.LifeOperationType.New) {
                // Life
                final KNode cell = createCell(graph, saveTime, lifeOperation.x, lifeOperation.y);
                ret = indexCell(graph, cell);
            } else {
                // Death
                ret = removeCell(graph, saveTime, lifeOperation.x, lifeOperation.y);
            }
            return ret;
        }).collect(Collectors.toList()).toArray(res);

        final DeferredManager barrierIndexes = new DefaultDeferredManager();
        return barrierIndexes.when(res);
    }

    private static Promise<Boolean, Object, Object> removeCell(KGraph graph, long saveTime, long x, long y) {
        final Promise<Boolean, Object, Object> res = lookupCellByCoordinates(graph, saveTime, x, y)
                .then((DonePipe<KNode, Boolean, Object, Object>) result -> {
                    final Deferred<Boolean, Object, Object> ret = new DeferredObject<>();
                    if(result == null) {
                        System.out.println("Cell("+x+", "+y+") not found");
                    } else {
                        result.attSet("a", KType.LONG, 0L);
                    }
                    ret.resolve(true);
                    return ret;
                });
        return res;
    }

    private static Deferred<KNode, Object, Object> lookupCellByCoordinates(final KGraph graph, final long saveTime, final long x, final long y) {
        final Deferred<KNode, Object, Object> deferred = new DeferredObject<>();
        final String query = "x=" + x + ",y=" + y+",a="+1L;
        graph.find(0, saveTime, "cells", query, deferred::resolve);
        return deferred;
    }

    private static Deferred<KNode[], Object, Object> getAllCells(KGraph graph, long time) {
        final Deferred<KNode[], Object, Object> ret = new DeferredObject<>();
        graph.all(0, time, "cells", ret::resolve);
        return ret;
    }

    private static KNode createCell(KGraph graph, long time, long x, long y) {
        System.out.println("at time " + x + " -> cell("+x+","+y+")");
        final KNode cell = graph.newNode(0, time);
        cell.attSet("x", KType.LONG, x);
        cell.attSet("y", KType.LONG, y);
        cell.attSet("a", KType.LONG, 1L);
        return cell;
    }

    private static Deferred<Boolean, Object, Object> indexCell(KGraph graph, KNode cell) {
        final Deferred<Boolean, Object, Object> ret = new DeferredObject<>();
        graph.index("cells", cell, new String[] {"x", "y", "a"}, ret::resolve);
        return ret;
    }

    private static Deferred<Boolean, Object, Object> save(KGraph graph) {
        final Deferred<Boolean, Object, Object> ret = new DeferredObject<>();
        graph.save(ret::resolve);
        return ret;
    }

    private static Deferred<Boolean, Object, Object> connect(KGraph graph) {
        final Deferred<Boolean, Object, Object> ret = new DeferredObject<>();
        graph.connect(ret::resolve);
        return ret;
    }
}

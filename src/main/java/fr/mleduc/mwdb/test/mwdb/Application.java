package fr.mleduc.mwdb.test.mwdb;


import org.jdeferred.*;
import org.jdeferred.impl.DefaultDeferredManager;
import org.jdeferred.impl.DeferredObject;
import org.jdeferred.multiple.MasterProgress;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;
import org.mwdb.*;
import org.mwdb.manager.NoopScheduler;

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
            //lifeOperations.add(LifeOperation.newCell(1,1));
            lifeOperations.add(LifeOperation.newCell(1,2));
            lifeOperations.add(LifeOperation.newCell(2,0));
            lifeOperations.add(LifeOperation.newCell(2,1));
            lifeOperations.add(LifeOperation.newCell(2,2));

            final Promise<Boolean, Object, Object> then = proceedLifesOperations(graph, 0, lifeOperations)
                    .then((MultipleResults result) -> save(graph));
            long lifeI = 1L;
            final Promise<Boolean, Object, Object> then2 = then
                    .then((Boolean result) -> getAll(graph, lifeI))
                    .then((CellGrid result) -> doLife(result))
                    .then((List<LifeOperation> result) -> proceedLifesOperations(graph, lifeI, result))
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
        DeferredObject<CellGrid, Object, Object> deferred = new DeferredObject<>();
        getAllCells(graph, time).then(result2 -> {
            final List<Cell> lstCells = Arrays.asList(result2).stream()
                    .map(kNode -> new Cell((long) kNode.att("x"), (long) kNode.att("y")))
                    .collect(Collectors.toList());
            deferred.resolve(new CellGrid(lstCells));
        });
        return deferred;
    }

    private static Promise<MultipleResults, OneReject, MasterProgress> proceedLifesOperations(final KGraph graph, final long saveTime, final List<LifeOperation> lifeOperations) {
        final Deferred[] res = new Deferred[lifeOperations.size()];
        lifeOperations.stream().map(lifeOperation -> {
            final KNode cell = createCell(graph, saveTime, lifeOperation.x, lifeOperation.y);
            return indexCell(graph, cell);
        }).collect(Collectors.toList()).toArray(res);

        final DeferredManager barrierIndexes = new DefaultDeferredManager();
        return barrierIndexes.when(res);
    }

    private static Deferred<KNode[], Object, Object> getAllCells(KGraph graph, long time) {
        final Deferred<KNode[], Object, Object> ret = new DeferredObject<>();
        graph.all(0, time, "cells", ret::resolve);
        return ret;
    }

    private static KNode createCell(KGraph graph, long time, long x, long y) {
        final KNode cell = graph.newNode(0, time);
        cell.attSet("x", KType.LONG, x);
        cell.attSet("y", KType.LONG, y);
        return cell;
    }

    private static Deferred<Boolean, Object, Object> indexCell(KGraph graph, KNode cell) {
        final Deferred<Boolean, Object, Object> ret = new DeferredObject<>();
        graph.index("cells", cell, new String[] {"x", "y"}, ret::resolve);
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

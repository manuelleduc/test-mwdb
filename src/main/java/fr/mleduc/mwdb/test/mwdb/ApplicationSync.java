package fr.mleduc.mwdb.test.mwdb;


import org.jdeferred.Deferred;
import org.jdeferred.DeferredManager;
import org.jdeferred.DonePipe;
import org.jdeferred.Promise;
import org.jdeferred.impl.DefaultDeferredManager;
import org.jdeferred.impl.DeferredObject;
import org.jdeferred.multiple.MasterProgress;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;
import org.mwdb.GraphBuilder;
import org.mwdb.KGraph;
import org.mwdb.KNode;
import org.mwdb.KType;
import org.mwdb.chunk.offheap.OffHeapChunkSpace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by mleduc on 17/03/16.
 */
public class ApplicationSync {
    public static void main(String[] args) throws InterruptedException {

        //Thread.sleep(30000);
        //Thread.sleep(3000);
        // 1 -> init a first list of LifeOperation (life only)
        // 2 -> persist it
        // 3 -> retrieve a cell grid <--------------+
        // 4 -> produce a serie of life actions     |
        // 5 -> persit it                           |
        // 6 -> repeat N time ----------------------+
        final long dim1 = 20;
        final long dim2 = 20;
        final int max = 80000;

        final long initialCapacity = (long) (dim1 * dim2 * 1.1);
        final int l = (int) (dim1 * dim2 * 1.1);
        final KGraph graph = GraphBuilder.builder()
                //.withScheduler(new NoopScheduler())
                .withSpace(new OffHeapChunkSpace(initialCapacity, l))
                .buildGraph();
        connect(graph);

        final List<LifeOperation> lifeOperations = new ArrayList<>();
       /* Random r = new Random();
        for (int i = 0; i < dim1; i++) {
            for (int j = 0; j < dim2; j++) {
                if (r.nextInt() % 100 > 75) {
                    lifeOperations.add(LifeOperation.newCell(i, j));
                }
            }
        }*/

        lifeOperations.add(LifeOperation.newCell(0,0));
        lifeOperations.add(LifeOperation.newCell(0,1));
        lifeOperations.add(LifeOperation.newCell(0,2));
        lifeOperations.add(LifeOperation.newCell(1,0));
        lifeOperations.add(LifeOperation.newCell(1,2));
        lifeOperations.add(LifeOperation.newCell(2,0));
        lifeOperations.add(LifeOperation.newCell(2,1));
        lifeOperations.add(LifeOperation.newCell(2,2));

        proceedLifeOperations(graph, 0, lifeOperations);
        save(graph);
        //final List<int> jksdfjksdf = new Arr
        for (int i = 1; i < max; i++) {
            //long start = System.currentTimeMillis();
            step(graph, i);

        }

        CellGrid c = getAllCells(graph, max);
        showState(max, c);

    }

    private static void step(KGraph graph, long lifeI) {
        CellGrid result = getAllCells(graph, lifeI);
        //showState(lifeI, result);
        List<LifeOperation> res2 = doLife(result);
        proceedLifeOperations(graph, lifeI, res2);
        save(graph);
    }

    private static void showState(long lifeI, CellGrid c) {
        System.out.println("State at " + lifeI);
        System.out.println(c);
    }

    private static List<LifeOperation> doLife(final CellGrid result) {
        final List<LifeOperation> lifeOperations = new GameOfLifeService().doLife(result);

        return lifeOperations;
    }

    private static KNode[] getAllNodes(final KGraph graph, final long time) {
        final KNode[][] ret = new KNode[1][1];
        graph.all(0, time, "cells", result -> {
            ret[0] = result;
        });
        return ret[0];
    }

    private static CellGrid getAllCells(final KGraph graph, final long time) {

        KNode[] result = getAllNodes(graph, time);

        final List<KNode> kNodes = Arrays.asList(result);

        final List<Cell> lstCells = kNodes.stream().filter(kNode -> kNode != null)
                .map(kNode -> {
                    final long x = (long) kNode.att("x");
                    final long y = (long) kNode.att("y");
                    final Cell cell = new Cell(x, y);
                    kNode.free();
                    return cell;
                }).collect(Collectors.toList());
        return new CellGrid(lstCells);
    }

    private static void proceedLifeOperations(final KGraph graph, final long time, final List<LifeOperation> lifeOperations) {

        lifeOperations.stream().forEach(lifeOperation -> {
            if (lifeOperation.type == LifeOperation.LifeOperationType.New) {
                // Life
                final KNode cell = createCell(graph, time, lifeOperation.x, lifeOperation.y);
                indexCell(graph, cell);
                cell.free();
            } else {
                // Death
                removeCell(graph, time, lifeOperation.x, lifeOperation.y);
            }
        });


    }

    private static void removeCell(final KGraph graph, final long saveTime, final long x, final long y) {
        final KNode cell = lookupCellByCoordinates(graph, saveTime, x, y);

        if (cell == null) {
            System.out.println("Cell(" + x + ", " + y + ") not found");
        } else {
            graph.unindex("cells", cell, new String[]{"x", "y"}, null);
        }
        cell.free();

    }

    private static KNode lookupCellByCoordinates(final KGraph graph, final long saveTime, final long x, final long y) {
        final String query = "x=" + x + ",y=" + y;
        final KNode[] ret = new KNode[1];
        graph.find(0, saveTime, "cells", query, (cell) -> {
            ret[0] = cell[0];
        });
        return ret[0];
    }

    private static KNode createCell(final KGraph graph, long time, long x, long y) {
        final KNode cell = graph.newNode(0, time);
        cell.attSet("x", KType.LONG, x);
        cell.attSet("y", KType.LONG, y);
        return cell;
    }

    private static void indexCell(final KGraph graph, KNode cell) {
        graph.index("cells", cell, new String[]{"x", "y"}, null);
    }

    private static void save(final KGraph graph) {
        graph.save(null);
    }

    private static void connect(final KGraph graph) {
        graph.connect(null);
    }
}
package fr.mleduc.mwdb.test.mwdb;


import etm.core.configuration.BasicEtmConfigurator;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;
import etm.core.renderer.SimpleTextRenderer;
import org.mwg.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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



        BasicEtmConfigurator.configure();
        final EtmMonitor monitor = EtmManager.getEtmMonitor();
        monitor.start();

        final long dim1 = 20;
        final long dim2 = 20;
        final int max = 20000;
        final int itts = 5;

        for(int i = 0; i< itts; i++) {
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
                .withOffHeapMemory()
                .withStorage(new RocksDBStorage("./itt-"+iterationLoop))
                .build();
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
            EtmPoint point2 = monitor.createPoint("loop");
            step(graph, i);
            point2.collect();

        }

        CellGrid c = getAllCells(graph, max);
        point.collect();
        showState(max, c);
        System.out.println("Stop " + iterationLoop);
    }

    private static void step(Graph graph, long lifeI) {
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

    private static Node[] getAllNodes(final Graph graph, final long time) {
        final Node[][] ret = new Node[1][1];
        graph.all(0, time, "cells", result -> {
            ret[0] = result;
        });
        return ret[0];
    }

    private static CellGrid getAllCells(final Graph graph, final long time) {

        Node[] result = getAllNodes(graph, time);

        final List<Node> kNodes = Arrays.asList(result);

        final List<Cell> lstCells = kNodes.stream().filter(kNode -> kNode != null)
                .map(kNode -> {
                    final long x = (long) kNode.get("x");
                    final long y = (long) kNode.get("y");
                    final Cell cell = new Cell(x, y);
                    kNode.free();
                    return cell;
                }).collect(Collectors.toList());
        return new CellGrid(lstCells);
    }

    private static void proceedLifeOperations(final Graph graph, final long time, final List<LifeOperation> lifeOperations) {

        lifeOperations.stream().forEach(lifeOperation -> {
            if (lifeOperation.type == LifeOperation.LifeOperationType.New) {
                // Life
                final Node cell = createCell(graph, time, lifeOperation.x, lifeOperation.y);
                indexCell(graph, cell);
                cell.free();
            } else {
                // Death
                removeCell(graph, time, lifeOperation.x, lifeOperation.y);
            }
        });


    }

    private static void removeCell(final Graph graph, final long saveTime, final long x, final long y) {
        final Node cell = lookupCellByCoordinates(graph, saveTime, x, y);

        if (cell == null) {
            System.out.println("Cell(" + x + ", " + y + ") not found");
        } else {
            graph.unindex("cells", cell, new String[]{"x", "y"}, null);
        }
        cell.free();

    }

    private static Node lookupCellByCoordinates(final Graph graph, final long saveTime, final long x, final long y) {
        final String query = "x=" + x + ",y=" + y;
        final Node[] ret = new Node[1];
        graph.find(0, saveTime, "cells", query, (cell) -> {
            ret[0] = cell[0];
        });
        return ret[0];
    }

    private static Node createCell(final Graph graph, long time, long x, long y) {
        final Node cell = graph.newNode(0, time);
        cell.setProperty("x", Type.LONG, x);
        cell.setProperty("y", Type.LONG, y);
        return cell;
    }

    private static void indexCell(final Graph graph, Node cell) {
        graph.index("cells", cell, new String[]{"x", "y"}, null);
    }

    private static void save(final Graph graph) {
        graph.save(null);
    }

    private static void connect(final Graph graph) {
        graph.connect(null);
    }
}

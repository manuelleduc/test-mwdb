package fr.mleduc.mwdb.test.mwdb;

import org.junit.Test;
import org.mwg.*;

/**
 * Created by mleduc on 21/03/16.
 */
public class CallbackFail {

    @Test
    public void test0() {
        final Graph graph = GraphBuilder.builder().build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                final Node node0 = graph.newNode(0, 0);
                node0.setProperty("x", Type.INT, 0);
                System.out.println("before index xs node0");
                graph.index("xs", node0, new String[]{"x"}, new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        System.out.println("callback index xs node0");
                        System.out.println("before save");
                        graph.save(new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                System.out.println("callback save");
                                System.out.println("before all 1");
                                graph.all(0, 1, "xs", new Callback<Node[]>() {
                                    @Override
                                    public void on(Node[] result) {
                                        System.out.println("callback all 1");
                                        System.out.println("before unindex");
                                        graph.unindex("xs", result[0], new String[]{"x"}, new Callback<Boolean>() {
                                            @Override
                                            public void on(Boolean result) {
                                                System.out.println("callback unindex");
                                                final Node node1 = graph.newNode(0, 1);
                                                node1.setProperty("x", Type.INT, 1);
                                                System.out.println("before index xs node1");
                                                graph.index("xs", node1, new String[]{"x"}, new Callback<Boolean>() {
                                                    @Override
                                                    public void on(Boolean result) {
                                                        System.out.println("callback index xs node1");
                                                        System.out.println("before all 2");
                                                        graph.all(0, 2, "xs", result1 -> {
                                                            System.out.println("callback all 2");
                                                            System.out.println("OK " + result1);
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    @Test
    public void test1() {
        final Graph graph = GraphBuilder.builder().build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                final Node node0 = graph.newNode(0, 0);
                node0.setProperty("x", Type.INT, 0);
                System.out.println("before index xs node0");
                graph.index("xs", node0, new String[]{"x"}, new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        System.out.println("callback index xs node0");
                        System.out.println("before save");
                        graph.save(new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                System.out.println("callback save");
                                System.out.println("before all 1");
                                graph.all(0, 1, "xs", new Callback<Node[]>() {
                                    @Override
                                    public void on(Node[] result) {
                                        System.out.println("callback all 1");

                                        final Node node1 = graph.newNode(0, 1);
                                        node1.setProperty("x", Type.INT, 1);
                                        System.out.println("before index xs node1");
                                        graph.index("xs", node1, new String[]{"x"}, new Callback<Boolean>() {
                                            @Override
                                            public void on(Boolean result) {
                                                System.out.println("callback index xs node1");
                                                System.out.println("before all 2");
                                                graph.all(0, 2, "xs", result1 -> {
                                                    System.out.println("callback all 2");
                                                    System.out.println("OK " + result1);
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }
}

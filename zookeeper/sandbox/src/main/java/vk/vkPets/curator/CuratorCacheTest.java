package vk.vkPets.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

// Either modern Curator cache or legacy TREE/PATH works
// Curator forAll cache listener does NOT see service removal!  forPathChildrenCache has to be used.
public class CuratorCacheTest {
    static final String ZOOKEEPER_ADDRESS = "localhost:2181"; // Replace with your ZooKeeper address
    static final String REGISTRY_ROOT = "/test";

    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    private static final String serviceName = "exampleService";

    private final List<ServerNode> nodes = new CopyOnWriteArrayList<>();
    private CuratorFramework watchingClient;

    @BeforeEach
    public void init() {
        watchingClient = CuratorFrameworkFactory.newClient(ZOOKEEPER_ADDRESS,
                new ExponentialBackoffRetry(1000, 3));
        watchingClient.start();
    }

    @AfterEach
    public void close() {
        watchingClient.close();
        nodes.forEach(ServerNode::close);
        exec.shutdown();
    }


    @Test
    public void testPathCache() throws Exception {
        String servicePath = REGISTRY_ROOT + "/" + serviceName;
        Instant startTime = Instant.now();
        try (PathChildrenCache cache = new PathChildrenCache(watchingClient, servicePath, true)) {
            cache.getListenable().addListener((client, event) -> {
                String result = switch (event.getType()) {
                    case CHILD_ADDED -> "added: " + durationSince(startTime)
                            + ". Path: " + event.getData().getPath()
                            + ". Data: " + formatData(event.getData().getData());
                    case CHILD_UPDATED -> "updated: " + durationSince(startTime)
                            + ". Path: " + event.getData().getPath()
                            + ". Data: " + formatData(event.getData().getData());
                    case CHILD_REMOVED -> "removed: " + durationSince(startTime)
                            + ". Path: " + event.getData().getPath();
                    default -> "UNKNOWN " + event;
                };

                System.out.println("-> " + result);
            });

            cache.start();

            String nodePath = servicePath + "/" + ServerNode.HOST + ":" + ServerNode.SEQUENCE.get();
            scheduleExamineCache(() -> cache.getCurrentData(nodePath));

            doChanges();
        }
    }

    @Test
    public void testTreeCache() throws Exception {
        // Watch the service nodes and list the available instances
        String servicePath = REGISTRY_ROOT + "/" + serviceName;
        Instant startTime = Instant.now();
        try (TreeCache treeCache = new TreeCache(watchingClient, servicePath)) {
            treeCache.getListenable().addListener((curatorFramework, event) -> {
                String result = switch (event.getType()) {
                    case NODE_ADDED -> "added:  " + durationSince(startTime)
                            + ". Path: " + event.getData().getPath()
                            + ". Data: " + formatData(event.getData().getData());
                    case NODE_UPDATED -> "updated:  " + durationSince(startTime)
                            + ". Path: " + event.getData().getPath()
                            + ". Data: " + formatData(event.getData().getData());
                    case NODE_REMOVED -> "removed:  " + durationSince(startTime)
                            + ". Path: " + event.getData().getPath();
                    default -> "UNKNOWN " + event;
                };

                System.out.println("-> " + result);
            });
            treeCache.start();

            String nodePath = servicePath + "/" + ServerNode.HOST + ":" + ServerNode.SEQUENCE.get();
            scheduleExamineCache(() -> treeCache.getCurrentData(nodePath));

            doChanges();
        }
    }

    @Test
    public void testCuratorCache() throws Exception {
        // Watch the service nodes and list the available instances
        String servicePath = REGISTRY_ROOT + "/" + serviceName;
        Instant startTime = Instant.now();
        try (CuratorCache curatorCache = CuratorCache.build(watchingClient, servicePath)) {
            // Register listener
            CuratorCacheListener listener = CuratorCacheListener.builder()
                    .forAll((type, oldData, data) -> {
                        String result = switch (type) {
                            case NODE_CREATED -> "added:  " + durationSince(startTime)
                                    + ". Path: " + data.getPath()
                                    + ". Data: " + formatData(data.getData());
                            case NODE_CHANGED -> "changed:  " + durationSince(startTime)
                                    + ". Path: " + data.getPath()
                                    + ". Data: " + formatData(data.getData());
                            case NODE_DELETED -> "removed:  " + durationSince(startTime)
                                    + ". Path: " + data.getPath();
                            default -> "UNKNOWN " + type;
                        };
                        System.out.println("ALL -> " + result);
                    }).build();

            curatorCache.listenable().addListener(listener);

            CuratorCacheListener childrenListener = CuratorCacheListener.builder()
                    .forPathChildrenCache(servicePath, watchingClient, ((client, event) -> {
                        String result = switch (event.getType()) {
                            case CHILD_ADDED -> "added: " + durationSince(startTime)
                                    + ". Path: " + event.getData().getPath()
                                    + ". Data: " + formatData(event.getData().getData());
                            case CHILD_UPDATED -> "updated: " + durationSince(startTime)
                                    + ". Path: " + event.getData().getPath()
                                    + ". Data: " + formatData(event.getData().getData());
                            case CHILD_REMOVED -> "removed: " + durationSince(startTime)
                                    + ". Path: " + event.getData().getPath();
                            default -> "UNKNOWN " + event;
                        };

                        System.out.println("PATH CHILDREN -> " + result);
            })).build();

            curatorCache.listenable().addListener(childrenListener);

            curatorCache.start();

            String nodePath = servicePath + "/" + ServerNode.HOST + ":" + ServerNode.SEQUENCE.get();
            scheduleExamineCache(() -> curatorCache.get(nodePath));

            doChanges();
        }
    }

    @Test
    public void testRequestNodeData() throws Exception {
        ServerNode node = registryNode("A");

        String nodePath = node.getNodePath();
        byte[] data = watchingClient.getData().forPath(nodePath);
        System.out.println("Result: " + formatData(data));
    }


    private void scheduleExamineCache(Supplier<Object> dataSupplier) {
        exec.schedule(() -> System.out.println("In cache after 3 sec: " + dataSupplier.get()),
                3, TimeUnit.SECONDS);
        exec.schedule(() -> System.out.println("In cache after 8 sec: " + dataSupplier.get()),
                8, TimeUnit.SECONDS);
        exec.schedule(() -> System.out.println("In cache after 13 sec: " + dataSupplier.get()),
                15, TimeUnit.SECONDS);
    }

    private void doChanges() throws Exception {
        Thread.sleep(1000);

        registryNode("");
        registryNode("AAA");
        registryNode("BBB");

        Thread.sleep(5000);
        System.out.println("\n\nupdating");

        nodes.get(0).update("CCC");
        nodes.get(1).update("DDD");
        nodes.get(2).update("EEE");

        Thread.sleep(3000);
        System.out.println("\n\ndeleting");
        nodes.get(2).unregister();

        Thread.sleep(2000);
        System.out.println("\n\nclosing curator client");
        stopNode();
        stopNode();
        stopNode();

        Thread.sleep(10_000);
        System.out.println("DONE");
    }


    private ServerNode registryNode(String data) throws Exception {
        ServerNode node = new ServerNode(serviceName);
        node.register(data + "," + new Date());
        nodes.add(node);
        return node;
    }

    private void stopNode() {
        ServerNode toRemove = nodes.remove(0);
        toRemove.close();
    }

    static String formatData(byte[] data) {
        return data == null ? "NULL" : new String(data);
    }

    static String durationSince(Instant startTime) {
        return Duration.between(Instant.now(), startTime).toString();
    }
}
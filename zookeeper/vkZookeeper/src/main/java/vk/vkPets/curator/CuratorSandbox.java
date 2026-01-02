package vk.vkPets.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheAccessor;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import vk.vkPets.LogUtil;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static vk.vkPets.LogUtil.log;
import static vk.vkPets.curator.CuratorCacheTest.ZOOKEEPER_ADDRESS;
import static vk.vkPets.curator.CuratorCacheTest.formatData;

public class CuratorSandbox {
    static CuratorFramework client;

    public static void main(String[] args) throws InterruptedException {
//        client = CuratorFrameworkFactory.newClient(CuratorCacheTest.ZOOKEEPER_ADDRESS,
//                new ExponentialBackoffRetry(1000, 3));
        client = CuratorFrameworkFactory.builder()
                .connectString(ZOOKEEPER_ADDRESS)
                .sessionTimeoutMs(15_000)
                .connectionTimeoutMs(5_000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();

        reportConnectionState();
        client.start();
//        log("started");


        String root = CuratorCacheTest.REGISTRY_ROOT;

        try (CuratorCache curatorCache = CuratorCache.build(client, root)) {
            CuratorCacheListener childrenListener = CuratorCacheListener.builder()
                    .forPathChildrenCache(root, client, ((client, event) -> {
                        String result = switch (event.getType()) {
                            case CHILD_ADDED -> "added"
                                    + ". Path: " + event.getData().getPath()
                                    + ". Data: " + formatData(event.getData().getData());
                            case CHILD_UPDATED -> "updated"
                                    + ". Path: " + event.getData().getPath()
                                    + ". Data: " + formatData(event.getData().getData());
                            case CHILD_REMOVED -> "removed"
                                    + ". Path: " + event.getData().getPath();
                            case INITIALIZED -> "INITIALIZED: " + event
                                    + "\nServices: " + fetchItems(curatorCache, CuratorCacheAccessor.parentPathFilter(CuratorCacheTest.REGISTRY_ROOT))
                                    + "\nNodes: " + fetchItems(curatorCache, Predicate.not(CuratorCacheAccessor.parentPathFilter(CuratorCacheTest.REGISTRY_ROOT)));
                            default -> "UNKNOWN " + event;
                        };

                        LogUtil.log(root + " -> " + result);
                    })).build();

            curatorCache.listenable().addListener(childrenListener);
            curatorCache.start();

            Thread.sleep(Long.MAX_VALUE);
        }
    }

    private static String fetchItems(CuratorCache curatorCache, Predicate<ChildData> predicate) {
        return curatorCache.stream()
                .filter(c -> !CuratorCacheTest.REGISTRY_ROOT.equals(c.getPath()))
                .filter(predicate)
                .map(ChildData::getPath)
                .collect(Collectors.joining(","));
    }

    private static void reportConnectionState() {
        client.getConnectionStateListenable().addListener((client, newState) -> {
            switch (newState) {
                case LOST -> log("Session lost.");
                case RECONNECTED -> log("Reconnected to ZooKeeper. Nodes: " + getNodes(ServerNode.FOO_SERVICE_NAME));
                case SUSPENDED -> log("Connection Suspended.");
                case CONNECTED -> log("Connected.");
                default -> log("UNEXPECTED: " + newState);
            }
        });
    }

    private static String getNodes(String serviceName) {
        try {
            List<String> children = client.getChildren().forPath(CuratorCacheTest.REGISTRY_ROOT + "/" + serviceName);
            return children.toString();
        } catch (Exception e) {
            log(e);
            return "";
        }
    }
}

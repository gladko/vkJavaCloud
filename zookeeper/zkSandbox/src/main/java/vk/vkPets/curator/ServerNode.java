package vk.vkPets.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static vk.vkPets.curator.CuratorCacheTest.REGISTRY_ROOT;
import static vk.vkPets.curator.CuratorCacheTest.ZOOKEEPER_ADDRESS;
import static vk.vkPets.LogUtil.*;

public class ServerNode implements AutoCloseable {
    static final AtomicInteger SEQUENCE = new AtomicInteger(10000);
    static final String HOST = "127.0.0.1";

    final int port;
    final String serviceName;
    final CuratorFramework curatorClient;

    public ServerNode(String serviceName) {
        this(serviceName, SEQUENCE.getAndIncrement());
    }

    public ServerNode(String serviceName, int port) {
        this.serviceName = serviceName;
        this.port = port;

        curatorClient = CuratorFrameworkFactory.builder()
                .connectString(ZOOKEEPER_ADDRESS)
                .sessionTimeoutMs(15_000)
//                .connectionTimeoutMs(connectionTimeoutMs)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();

        maintain();
        curatorClient.start();
    }

    public void register(String data) throws Exception {
        String nodePath = getNodePath();

        Object result;
        if (data == null) {
            result = curatorClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(nodePath);
        } else {
            result = curatorClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(nodePath, data.getBytes());
        }

        log("\nService node registered. Path: " + nodePath + ". Result: " + result);
    }

    public void update(String data) throws Exception {
        String nodePath = getNodePath();
        Object result = curatorClient.setData().forPath(nodePath, data.getBytes());
        log("\nService node changed. Path: " + nodePath + ". Result: " + result);
    }

    @Override
    public void close() {
        curatorClient.close();
    }

    AtomicBoolean lost = new AtomicBoolean();
    private void maintain() {
        curatorClient.getConnectionStateListenable().addListener((client, newState) -> {
            switch (newState) {
                case LOST:
                    log("Session lost. Ephemeral nodes deleted.");
                    // FROM ChatGPT: re-create ephemeral nodes or restart tasks here
                    // NOT ACCURATE!!!
                    lost.set(true);
                    break;
                case CONNECTED:
                    log("Connected to ZooKeeper.");
                case RECONNECTED:
                    log("Reconnected to ZooKeeper.");
                    // FROM ChatGPT: ephemeral nodes exist only if session not lost, otherwise must recreate
                    // NOT ACCURATE!!!
                    try {
                        if (lost.getAndSet(false)) {
                            register("");
                        }
                    } catch (Exception e) {
                        log(e.toString());
                    }
                    break;
                case SUSPENDED:
                    log("Connection Suspended.");
                    break;
            }
        });
    }

    public void unregister() throws Exception {
        curatorClient.delete().forPath(getNodePath());
    }

    @NotNull
    public String getNodePath() {
        return REGISTRY_ROOT + "/" + serviceName + "/" + HOST + ":" + port;
    }

    static final String FOO_SERVICE_NAME = "foo";
    public static void main(String[] args) throws Exception {
        log("starting");
        int port = ThreadLocalRandom.current().nextInt(10000);
        try (ServerNode serverNode = new ServerNode(FOO_SERVICE_NAME, port)) {
//            serverNode.maintain();

            serverNode.register(new Date().toString());
            log("registered");

            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            log(e.toString());
            throw e;
        }
    }
}
package vk.vkPets.server;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Observer implements AutoCloseable {
    public static void main(String[] args) throws Exception {
        new Observer(LoggerFactory.getLogger(Observer.class));
        Thread.sleep(Long.MAX_VALUE);
    }

    private final Logger logger;

    private final Client etcdClient = Client.builder().endpoints(NodesMain.ETCD_ENDPOINT).build();
    private final ConcurrentHashMap<UUID, NodeData> clusterMembers = new ConcurrentHashMap<>();
    private Watch.Watcher watcher;

    public Observer(Logger logger) throws Exception {
        this.logger = logger;
        long maxModRevision = loadMembershipSnapshot();
        watchMembershipChanges(maxModRevision);
    }

    public Collection<NodeData> getClusterMembers() {
        return Collections.unmodifiableCollection(clusterMembers.values());
    }

    private long loadMembershipSnapshot() throws Exception {
        // TODO handle pagination?
        GetResponse response = etcdClient.getKVClient().get(
                ByteSequence.from(Node.NODES_PREFIX, StandardCharsets.UTF_8),
                GetOption.builder()
//                        .withPrefix(ByteSequence.from(NODES_PREFIX, StandardCharsets.UTF_8))
                        .isPrefix(true)
                        .build()
        ).get(Node.OPERATION_TIMEOUT, TimeUnit.SECONDS);

        for (KeyValue kv : response.getKvs()) {
            NodeData nodeData = JsonObjectMapper.read(kv.getValue().toString(StandardCharsets.UTF_8));
            logger.info("LOAD {}", nodeData);
            clusterMembers.put(nodeData.getUuid(), nodeData);
        }
        return response.getKvs().stream()
                .mapToLong(KeyValue::getModRevision).max().orElse(0);
    }

    private void watchMembershipChanges(long fromRevision) {
        logger.info("Watching membership changes from revision {}", fromRevision);
        watcher = etcdClient.getWatchClient().watch(
                ByteSequence.from(Node.NODES_PREFIX, StandardCharsets.UTF_8),
                WatchOption.builder()
//                        .withPrefix(ByteSequence.from(NODES_PREFIX, StandardCharsets.UTF_8))
                        .isPrefix(true)
                        .withRevision(fromRevision + 1)
                        .build(),
                watchResponse -> {
                    // TODO handle watch response on separate executor to not block grpc-default-executor
                    watchResponse.getEvents().forEach(this::handleWatchEvent);
                },
                error -> logger.error("Watcher broke", error),
                () -> logger.info("Watcher completed")
        );
    }

    private void handleWatchEvent(WatchEvent watchEvent) {
        try {
            switch (watchEvent.getEventType()) {
                case PUT -> {
                    String nodeValue = watchEvent.getKeyValue().getValue().toString(StandardCharsets.UTF_8);
                    NodeData nodeData = JsonObjectMapper.read(nodeValue);
                    logger.info("on PUT {}", nodeData);
                    clusterMembers.put(nodeData.getUuid(), nodeData);
                }
                case DELETE -> {
                    String etcdKey = watchEvent.getKeyValue().getKey().toString(StandardCharsets.UTF_8);
                    UUID nodeUuid = UUID.fromString(extractNodeUuid(etcdKey));
                    logger.info("on DELETE {}", nodeUuid);
                    clusterMembers.remove(nodeUuid);
                }
                default -> logger.warn("Unrecognized event: {}", watchEvent.getEventType());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to handle watch event", e);
        }
    }

    private String extractNodeUuid(String etcdKey) {
        return etcdKey.replaceAll(Pattern.quote(Node.NODES_PREFIX), "");
    }

    @Override
    public void close() {
        watcher.close();
        etcdClient.close();
    }
}

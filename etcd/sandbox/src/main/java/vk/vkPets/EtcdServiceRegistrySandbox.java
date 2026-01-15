package vk.vkPets;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.support.CloseableClient;
import io.etcd.jetcd.watch.WatchEvent;
import io.grpc.stub.StreamObserver;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class EtcdServiceRegistrySandbox {
    private static final String ETCD_ENDPOINT = "https://localhost:2379";
//    private static final String ETCD_ENDPOINT = "https://localhost:12379";
    private static final String SERVICES_PREFIX = "/services/";
    private static final String serviceName = "my-service";
    private static final long leaseTtl = 5; // seconds


    public static void main(String[] args) throws Exception {
        Client client = Client.builder().endpoints(ETCD_ENDPOINT).build();
        Watch.Watcher watcher = watchServices(client);

        Node node1 = new Node("instance-1", "127.0.0.1:8080");
        Node node2 = new Node("instance-2", "127.0.0.2:8080");
        Node node3 = new Node("instance-3", "127.0.0.3:8080");

        Thread.sleep(5_000);
        System.out.println("Discovering services 1: " + discoverServices(client, serviceName));

        node1.close();
        Thread.sleep(5_000);
        System.out.println("Discovering services 2: " + discoverServices(client, serviceName));

        Thread.sleep(5000_000);
        watcher.close();
        client.close();
    }

    private static Watch.Watcher watchServices(Client client) {
        return client.getWatchClient().watch(servicePrefix(serviceName),
                WatchOption.builder().isPrefix(true).build(),
                watchResponse -> {
                    // TODO handle watch response on separate executor to not block grpc-default-executor
                    watchResponse.getEvents().forEach(EtcdServiceRegistrySandbox::handleWatchEvent);
                },
                error -> System.out.println("Watcher broke: " + error),
                () -> System.out.println("Watcher completed")
        );
    }

    private static void handleWatchEvent(WatchEvent watchEvent) {
        try {
            switch (watchEvent.getEventType()) {
                case PUT -> {
                    String nodeValue = watchEvent.getKeyValue().getValue().toString(StandardCharsets.UTF_8);
                    System.out.println("watcher on PUT: " + nodeValue);
                }
                case DELETE -> {
                    String etcdKey = watchEvent.getKeyValue().getKey().toString(StandardCharsets.UTF_8);
                    System.out.println("watcher on DELETE: " + etcdKey);
                }
                default ->  System.out.println("Unrecognized event: " + watchEvent.getEventType());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to handle watch event", e);
        }
    }


    public static List<String> discoverServices(Client client, String serviceName) throws Exception {
        GetResponse response = client.getKVClient()
                .get(servicePrefix(serviceName),
                        // GetOption.builder().withPrefix(servicePrefix).build())
                        GetOption.builder().isPrefix(true).build())
                .get();
        return response.getKvs().stream()
                .map(KeyValue::getValue)
                .map(bs -> bs.toString(StandardCharsets.UTF_8))
                .collect(Collectors.toList());
    }

    private static ByteSequence key(String serviceName, String instanceId) {
        return ByteSequence.from((SERVICES_PREFIX + serviceName + "/" + instanceId).getBytes(StandardCharsets.UTF_8));
    }

    private static ByteSequence servicePrefix(String serviceName) {
        return ByteSequence.from((SERVICES_PREFIX + serviceName + "/").getBytes(StandardCharsets.UTF_8));
    }

    static class Node {
        private final Client client;
        final String instanceId;
        final String address;
        CloseableClient keepAliveListener;
        long leaseId;

        public Node(String instanceId, String address) throws Exception {
            this.client = Client.builder().endpoints(ETCD_ENDPOINT).build();;
            this.instanceId = instanceId;
            this.address = address;

            registerService();
        }

        public void registerService() throws Exception {
            // Create a lease with a TTL so that the service key will expire if not renewed (for detecting dead instances)
            leaseId = client.getLeaseClient().grant(leaseTtl).get().getID();

            // Keep alive the lease to maintain registration
            keepAliveListener = client.getLeaseClient().keepAlive(leaseId, new StreamObserver<>() {
                @Override
                public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {
                    System.out.println("Kept lease alive: " + leaseId);
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println("Failed to keep lease alive: " + leaseId);
                }

                @Override
                public void onCompleted() {
                    System.out.println("Lease completed: " + leaseId);
                }
            });

            PutResponse putResponse = client.getKVClient().put(
                            key(serviceName, instanceId),
                            ByteSequence.from(address.getBytes(StandardCharsets.UTF_8)),
                            PutOption.builder().withLeaseId(leaseId).build()) // Put key with lease
                    .get();
            System.out.println("putResponse: " + putResponse);
        }

        public void close() throws Exception {
            client.getKVClient().delete(key(serviceName, instanceId)).get();
            keepAliveListener.close();
            client.close();

            System.out.println("Closed " + instanceId + " with lease " + leaseId);
        }
    }
}

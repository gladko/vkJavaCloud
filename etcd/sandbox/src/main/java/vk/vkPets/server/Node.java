package vk.vkPets.server;


import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.support.CloseableClient;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Node implements AutoCloseable {
    public static void main(String[] args) throws Exception {
        Node node = new Node(NodesMain.ETCD_ENDPOINT);
        node.join();

        Thread.sleep(Long.MAX_VALUE);
    }

    private final Logger logger;

    static final int OPERATION_TIMEOUT = 5;
    private static final long DEFAULT_LEASE_TTL = 15;
    public static final String NODES_PREFIX = "/nodes/";

    private final NodeData nodeData;

    private final Client etcdClient;

    private final long leaseTtl;
    private volatile long leaseId;
    private volatile CloseableClient keepAliveClient;



    public Node(List<URI> endpoints) throws Exception {
        this(endpoints, DEFAULT_LEASE_TTL);
    }

    public Node(List<URI> endpoints, long leaseTtl) throws Exception {
        this.leaseTtl = leaseTtl;
        nodeData = new NodeData(UUID.randomUUID());
        logger = LoggerFactory.getLogger(nodeData.getUuid().toString());

        logger.info("Connecting to etcd on the following endpoints: {}", endpoints);
        etcdClient = Client.builder().endpoints(endpoints).build();
    }

    public void join() throws Exception {
        try {
            logger.info("Joining the cluster");
            grantLease();
            putMetadata();
            logger.info("Join complete");
        } catch (Exception e) {
            throw new Exception(String.format("Node %s failed to join.", nodeData.getUuid()), e);
        }
    }

    private void grantLease() throws Exception {
        Lease leaseClient = etcdClient.getLeaseClient();
        logger.info("Granting lease");
        leaseClient.grant(leaseTtl)
                .thenAccept((leaseGrantResponse -> {
                    leaseId = leaseGrantResponse.getID();
                    logger.info("Lease {} granted", leaseId);
                    keepAliveClient = leaseClient.keepAlive(leaseId,
                            new StreamObserver<>() {
                                @Override
                                public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {
                                    logger.debug("Kept lease {} alive", leaseId);
                                    logger.info("Kept lease {} alive", leaseId);
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    logger.error("Failed to keep lease {} alive", leaseId);
                                }

                                @Override
                                public void onCompleted() {
                                    logger.debug("Lease completed");
                                }
                            });
                })).get(OPERATION_TIMEOUT, TimeUnit.SECONDS);
    }

    private void putMetadata() throws Exception {
        logger.info("Putting node metadata");
        etcdClient.getKVClient().put(
                ByteSequence.from(
                        NODES_PREFIX + nodeData.getUuid(),
                        StandardCharsets.UTF_8
                ),
                ByteSequence.from(
                        JsonObjectMapper.write(nodeData),
                        StandardCharsets.UTF_8
                ),
                PutOption.builder().withLeaseId(leaseId).build()
        ).get(OPERATION_TIMEOUT, TimeUnit.SECONDS);
    }

    public void leave() throws LeaveFailedException {
        try {
            logger.info("Leaving the cluster");
            if (keepAliveClient != null) {
                keepAliveClient.close();
            }
            etcdClient.getLeaseClient().revoke(leaseId)
                    .get(OPERATION_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new LeaveFailedException(nodeData, e);
        }
    }

    public NodeData getNodeData() {
        return nodeData;
    }

    @Override
    public void close(){
        try {
            leave();
        } catch (LeaveFailedException e) {
            logger.error(e.toString());
        }
        etcdClient.close();
    }
}
package vk.vkPets;


import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.toxiproxy.ToxiproxyContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import vk.vkPets.server.LeaveFailedException;
import vk.vkPets.server.Node;
import vk.vkPets.server.Observer;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
@Testcontainers
class NodeTest {
    static Logger logger = LoggerFactory.getLogger(NodeTest.class);

    private static final String ETCD_DOCKER_IMAGE_NAME = "gcr.io/etcd-development/etcd:v3.6.7";
    private static final Network network = Network.newNetwork();
    private static final int ETCD_PORT = 2379;

//    private ToxiproxyContainer etcdProxy;
    private Proxy proxy;

    @AfterAll
    public static void afterAll() {
        network.close();
        etcd.close();
        toxiproxy.close();
    }

    @Container
    private static final GenericContainer<?> etcd =
//            new GenericContainer<>(EtcdContainer.ETCD_DOCKER_IMAGE_NAME)
            new GenericContainer<>(ETCD_DOCKER_IMAGE_NAME)
                    .withCommand("etcd",
                            "--listen-client-urls", "http://0.0.0.0:" + ETCD_PORT,
                            "--advertise-client-urls", "http://0.0.0.0:" + ETCD_PORT,
                            "--name", NodeTest.class.getSimpleName())
                    .withExposedPorts(ETCD_PORT)
                    .withNetwork(network)
                    .withNetworkAliases("etcd");;

    @Container
    public static final ToxiproxyContainer toxiproxy =
            new ToxiproxyContainer("shopify/toxiproxy:2.1.0")
                    .withNetwork(network)
                    .withNetworkAliases("toxiproxy");

    @BeforeEach
    public void beforeEach() throws IOException {
        ToxiproxyClient toxiproxyClient = new ToxiproxyClient(toxiproxy.getHost(), toxiproxy.getControlPort());
        proxy = toxiproxyClient.createProxy("etcd", "0.0.0.0:8666", "etcd:2379");

//        etcdProxy = toxiproxy.getProxy(etcd, ETCD_PORT);
    }

    private List<URI> getClientEndpoints() {
        return List.of(URI.create(
                "https://" + etcd.getHost() +
                        ":" + etcd.getMappedPort(ETCD_PORT)
        ));
    }

    private List<URI> getProxiedClientEndpoints() {
//        return List.of(URI.create(
//                "https://" + etcdProxy.getHost() +
//                        ":" + etcdProxy.getMappedPort(8666)
//        ));
        return List.of(URI.create("localhost:8666"));
    }

    @Test
    public void testTwoNodesJoinLeave() throws Exception {
        List<URI> endpoints = getClientEndpoints();
        try (Observer observer = new Observer(logger);
                Node node1 = new Node(endpoints))
        {
            node1.join();
            try (Node node2 = new Node(endpoints)) {
                node2.join();

                Awaitility.await("See both nodes").until(() -> observer.getClusterMembers()
                      .containsAll(List.of(node1.getNodeData(), node2.getNodeData())));
            }
            Awaitility.await("See that node2 is gone").until(() -> observer.getClusterMembers()
                      .equals(Set.of(node1.getNodeData())));
        }
    }

    @Test
    public void testTwoNodesLeaseExpires() throws Exception {
        long leaseTtl = 1;
        try (Observer observer = new Observer(logger);
                Node node1 = new Node(getClientEndpoints(), leaseTtl))
        {
            node1.join();
            try (Node node2 = new Node(getProxiedClientEndpoints(), leaseTtl)) {
                node2.join();

                Awaitility.await("See all nodes").until(() -> observer.getClusterMembers()
                                .containsAll(List.of(node1.getNodeData(), node2.getNodeData())));

                proxy.toxics().latency("latency", ToxicDirection.UPSTREAM, leaseTtl * 2000);

                Awaitility.await("See that node 2 is gone").until(() -> observer.getClusterMembers()
                                .equals(Set.of(node1.getNodeData())));
            } catch (LeaveFailedException e) {
//                assertThat(e).hasStackTraceContaining("requested lease not found");
                System.out.println("EXPECTED!!!");
            }
        }
    }

    @Disabled
    @Test
    public void testLargerCluster() throws Exception {
        int clusterSize = 100;
        List<Node> cluster = Stream.generate(() -> {
            try {
                return new Node(getClientEndpoints());
            } catch (Exception e) {
                return null;
            }
        }).limit(clusterSize).toList();

        assertEquals(clusterSize, cluster.size());

        try (Observer observer = new Observer(logger)) {
            for (Node node : cluster) {
                node.join();
            }

            Awaitility.await("Node 1 to see all other nodes")
                    .until(() -> observer.getClusterMembers().size() == clusterSize);
        } finally {
            for (Node node : cluster) {
                try {
                    node.close();
                } catch (Exception e) {
                    // doesn't matter
                }
            }
        }

    }

}
package vk.vkPets;


import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import io.etcd.jetcd.Client;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.toxiproxy.ToxiproxyContainer;
import vk.vkPets.server.LeaveFailedException;
import vk.vkPets.server.Node;
import vk.vkPets.server.Observer;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@Disabled
@Testcontainers
class NodeTest {
    static Logger logger = LoggerFactory.getLogger(NodeTest.class);

    protected List<URI> etcdEndpoints;

    static final String ETCD_DOCKER_IMAGE_NAME = "gcr.io/etcd-development/etcd:v3.6.7";
    private static final Network network = Network.newNetwork();
    private static final int ETCD_PORT = 2379;

    //    private ToxiproxyContainer etcdProxy;
    private static ToxiproxyClient toxiproxyClient;
    private static Proxy proxy;


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
        toxiproxyClient.reset();

//        etcdProxy = toxiproxy.getProxy(etcd, ETCD_PORT);

        etcdEndpoints = getEtcdEndpoints();
    }

    @BeforeAll
    public static void beforeAll() throws IOException {
        toxiproxyClient = new ToxiproxyClient(toxiproxy.getHost(), toxiproxy.getControlPort());
        proxy = toxiproxyClient.createProxy("etcd", "0.0.0.0:8666", "etcd:2379");
    }

    @AfterAll
    public static void afterAll() {
        network.close();
        etcd.close();
        toxiproxy.close();
    }

    protected List<URI> getEtcdEndpoints() {
        return List.of(URI.create(
                "https://" + etcd.getHost() + ":" + etcd.getMappedPort(ETCD_PORT)
//                "https://" + etcd.getContainerIpAddress() + ":" + etcd.getMappedPort(ETCD_PORT)));
        ));
    }

    private List<URI> getProxiedEtcdEndpoints() {
        return List.of(URI.create(
                "https://localhost:8666"
//                "https://" + proxy.getHost() + ":" + proxy.getMappedPort(8666)
//                "https://" + etcdProxy.getContainerIpAddress() + ":" + etcdProxy.getProxyPort()));
        ));
    }


    @Test
    public void testTwoNodesJoinLeave() throws Exception {
        long leaseTtl = 3;

        try (Node node1 = new Node(etcdEndpoints, leaseTtl)) {
            node1.join();
            try (Node node2 = new Node(etcdEndpoints, leaseTtl)) {
                node2.join();

                logger.info("checking all nodes ...");
                Awaitility.await("Node 1 to see all nodes")
                        .until(() -> node1.getClusterMembers()
                                .containsAll(List.of(node1.getNodeData(), node2.getNodeData())));
                Awaitility.await("Node 2 to see all nodes")
                        .until(() -> node2.getClusterMembers()
                                .containsAll(List.of(node1.getNodeData(), node2.getNodeData())));
                logger.info("checking all nodes DONE");
            }

            logger.info("checking Node 1 to see that node 2 is gone ... ");
            Awaitility.await("Node 1 to see that node 2 is gone")
                    .until(() -> node1.getClusterMembers()
                            .equals(Set.of(node1.getNodeData())));
        }
    }

    @Disabled
    @Test
    public void testTwoNodesLeaseExpires() throws Exception {
        long leaseTtl = 1;
        try (Node node1 = new Node(etcdEndpoints, leaseTtl)) {
            node1.join();
            try (Node node2 = new Node(getProxiedEtcdEndpoints(), leaseTtl)) {
                node2.join();

                Awaitility.await("See all nodes").until(() -> node1.getClusterMembers()
                                .containsAll(List.of(node1.getNodeData(), node2.getNodeData())));

                proxy.toxics().latency("latency", ToxicDirection.UPSTREAM, leaseTtl * 2000);

                Awaitility.await("See that node 2 is gone").until(() -> node1.getClusterMembers()
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

        List<Node> cluster = new ArrayList<>();
        for (int i = 0; i < clusterSize; i++) {
            cluster.add(new Node(etcdEndpoints));
        }

        assertEquals(clusterSize, cluster.size());

        try (Client etcdClient = Client.builder().endpoints(etcdEndpoints).build();
             Observer observer = new Observer(etcdClient, logger))
        {
            for (Node node : cluster) {
                node.join();
            }

            Awaitility.await("Node 1 to see all other nodes")
                    .until(() -> cluster.get(0).getClusterMembers().size() == clusterSize);

            Awaitility.await("Observer to see all other nodes")
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
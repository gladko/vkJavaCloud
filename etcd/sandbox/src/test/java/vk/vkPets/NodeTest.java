package vk.vkPets;

/*
@Testcontainers
class NodeTest {

    private static final Network network = Network.newNetwork();
    private static final int ETCD_PORT = 2379;

    private ToxiproxyContainer.ContainerProxy etcdProxy;

    @AfterAll
    private static void afterAll() {
        network.close();
    }

    @Container
    private static final GenericContainer<?> etcd =
            new GenericContainer<>(EtcdContainer.ETCD_DOCKER_IMAGE_NAME)
                    .withCommand("etcd",
                            "-listen-client-urls", "http://0.0.0.0:" + ETCD_PORT,
                            "--advertise-client-urls", "http://0.0.0.0:" + ETCD_PORT,
                            "--name", NodeTest.class.getSimpleName())
                    .withExposedPorts(ETCD_PORT)
                    .withNetwork(network);

    @Container
    public static final ToxiproxyContainer toxiproxy =
            new ToxiproxyContainer("shopify/toxiproxy:2.1.0")
                    .withNetwork(network)
                    .withNetworkAliases("toxiproxy");

    @BeforeEach
    public void beforeEach() {
        etcdProxy = toxiproxy.getProxy(etcd, ETCD_PORT);
    }

    private List<URI> getClientEndpoints() {
        return List.of(URI.create(
                "https://" + etcd.getContainerIpAddress() +
                        ":" + etcd.getMappedPort(ETCD_PORT)
        ));
    }

    private List<URI> getProxiedClientEndpoints() {
        return List.of(URI.create(
                "https://" + etcdProxy.getContainerIpAddress() +
                        ":" + etcdProxy.getProxyPort()
        ));
    }

    @Test
    public void testNodeJoin() throws Exception {
        try (Node node = new Node(getClientEndpoints())) {
            node.join();
        }
    }
}

 */
package vk.vkPets.xdiscovery;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.details.ServiceCacheListener;
import org.apache.curator.x.discovery.details.ServiceDiscoveryImpl;
import org.apache.curator.x.discovery.details.VkServiceCacheImpl;
import org.apache.curator.x.discovery.strategies.RandomStrategy;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;

public class XDiscoveryTest {
    private static final String ZOOKEEPER_ADDRESS = XDiscoverySandbox.ZOOKEEPER_ADDRESS;
    public static final String PATH = "/discovery/example";


    static ServiceDiscovery<NodeDetails> serviceDiscovery;
    static CuratorFramework client = null;
    static String fooServiceName = "FOO";
    static String barServiceName = "BAR";
    static List<XServerNode> servers = new ArrayList<>();

    @BeforeAll
    public static void beforeClass() throws Exception {
        // This method is scaffolding to get the example up and running
        client = CuratorFrameworkFactory.newClient(ZOOKEEPER_ADDRESS,
                new ExponentialBackoffRetry(1000, 3));
        System.out.println("client starting");
        client.start();
        System.out.println("client started");

        serviceDiscovery = ServiceDiscoveryBuilder.builder(NodeDetails.class)
                .client(client)
                .basePath(PATH)
                .serializer(new JsonInstanceSerializer<>(NodeDetails.class))
                .build();
        System.out.println("serviceDiscovery starting");
        serviceDiscovery.start();
        System.out.println("serviceDiscovery started");

        addServers(servers, fooServiceName, 3);
        addServers(servers, barServiceName, 5);
    }

    @AfterAll
    public static void clean() {
        servers.forEach(CloseableUtils::closeQuietly);
        CloseableUtils.closeQuietly(serviceDiscovery);
        CloseableUtils.closeQuietly(client);
    }

    private static void addServers(List<XServerNode> storage, String serviceName, int count) throws Exception {
        for (int i = 0; i < count; i++) {
            String description = "TEST_" + ThreadLocalRandom.current().nextInt();
            XServerNode server = new XServerNode(PATH, serviceName, description);
            server.start();
            storage.add(server);
        }
    }

    // 1. Stop zk
    // 2. Run this test
    // 3. Wait. How long? How long do services try to connect to zk and register?
    // 4. Start zk
    // 5. Check if services are registered and discovered
    @Test
    public void zkIsDown() throws Exception {
        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            // This listener DOES NOT notify about service node changes.
            // It notifies discoveryClient to zookeeper connection state changes
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                // Handle connection state changes if needed
                System.out.println("ZOOKEEPER CONNECTION ---> connection state changed! New State: " + newState);
            }
        });

        System.out.println("client blockUntilConnected");
        client.blockUntilConnected();
        System.out.println("client connected");
        listInstances();
    }

    @Test
    public void listInstances() throws Exception {
        Collection<String> serviceNames = serviceDiscovery.queryForNames();
        System.out.println("discovered " + serviceNames.size() + " service name(s)");
        for (String serviceName : serviceNames) {
            Collection<ServiceInstance<NodeDetails>> instances =
                    serviceDiscovery.queryForInstances(serviceName);
            System.out.println(serviceName + " nodes:");
            for (ServiceInstance<NodeDetails> instance : instances) {
                printInstance(instance);
            }
        }
    }

    /**
     * ServiceProvider may be considered as load balancer. Balancing algorithm may be set via {@link ProviderStrategy}.
     * In this example {@link RandomStrategy} is used.
     */
    @Test
    public void listRandomInstance() throws Exception {
        String serviceName = fooServiceName;
        try (ServiceProvider<NodeDetails> provider = serviceDiscovery.serviceProviderBuilder()
                .serviceName(serviceName)
                .providerStrategy(new RandomStrategy<>())
                .build())
        {
            provider.start();

            // give the provider time to warm up - in a real application you wouldn't need to do this
            Thread.sleep(2500);

            for (int i = 0; i < 10; i++) {
                ServiceInstance<NodeDetails> instance = provider.getInstance();
                printInstance(instance);
                Thread.sleep(100);
            }
        }
    }

    @Test
    public void listenStateChanges2() throws Exception {
        String serviceName = "TEST_UPDATES_SERVICE";
        List<XServerNode> testServers = new ArrayList<>();

        try (VkServiceCacheImpl cache = new VkServiceCacheImpl((ServiceDiscoveryImpl) serviceDiscovery,
                serviceName, Thread::new))
        {
            cache.start();
            action(cache, serviceName, testServers);

            Thread.sleep(500000);
            System.out.println("DONE");
        } finally {
            testServers.forEach(XServerNode::close);
        }
    }

    @Test
    public void listenStateChanges() throws Exception {
        String serviceName = "TEST_UPDATES_SERVICE";
        List<XServerNode> testServers = new ArrayList<>();

        try (ServiceCache<NodeDetails> cache = serviceDiscovery.serviceCacheBuilder()
                .name(serviceName)
                .build())
        {
            cache.addListener(new ServiceCacheListener() {
                @Override
                public void cacheChanged() {
                    List<ServiceInstance<NodeDetails>> instances = cache.getInstances();
                    // Compare this list to a previous snapshot to detect add/remove
                    System.out.println("SERVICE ---> instances changed! New count: " + instances.size());
                }

                // This listener DOES NOT notify about service node changes.
                // It notifies discoveryClient to zookeeper connection state changes
                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState) {
                    // Handle connection state changes if needed
                    System.out.println("ZOOKEEPER CONNECTION ---> connection state changed! New State: " + newState);
                }
            });

            cache.start();

            action(cache, serviceName, testServers);

            Thread.sleep(500000);
            System.out.println("DONE");
        } finally {
            testServers.forEach(XServerNode::close);
        }
    }

    private static void action(ServiceCache<NodeDetails> cache, String serviceName, List<XServerNode> testServers) throws Exception {
        System.out.println("startup cached instances: " + cache.getInstances());
        System.out.println("listening for " + PATH + ", service: " + serviceName);

        System.out.println("adding");
        addServers(testServers, serviceName, 3);

        Thread.sleep(5000);
        System.out.println("updating");
        testServers.get(0).update("bar");

        Thread.sleep(5000);
        System.out.println("closing");
        testServers.get(0).close();
        Thread.sleep(1000);
        testServers.get(1).close();
        Thread.sleep(1000);
        testServers.get(2).close();
    }

    private static void printInstance(ServiceInstance<NodeDetails> instance) {
        if (instance == null) {
            System.err.println("\tNULL");
        } else if (instance.getPayload() == null) {
            System.err.println("\tPAYLOAD is NULL");
        } else {
            System.out.println("\t" + instance.getPayload().getDescription() + ": " + instance.buildUriSpec());
        }
    }
}

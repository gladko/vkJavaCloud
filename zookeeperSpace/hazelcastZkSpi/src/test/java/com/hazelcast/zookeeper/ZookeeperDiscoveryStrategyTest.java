package com.hazelcast.zookeeper;

import com.hazelcast.cluster.Address;
import com.hazelcast.logging.StandardLoggerFactory;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import org.apache.curator.test.TestingServer;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;



public class ZookeeperDiscoveryStrategyTest {
    private static final String LOCAL_ZK = "localhost:2181";

    static StandardLoggerFactory loggerFactory = new StandardLoggerFactory();
    static TestingServer zkTestServer;

    public static void main(String[] args) throws Exception {
//        withTestServer();
//        new ZookeeperDiscoveryStrategyTest().runRegistry();
    }

    private static void withTestServer() throws Exception {
        zkTestServer = new TestingServer();
        String zookeeperURL = zkTestServer.getConnectString();

        DiscoveryNode node = new SimpleDiscoveryNode(new Address("localhost", 123));

        Map<String, Comparable> props = Map.of(ZookeeperDiscoveryProperties.ZOOKEEPER_URL.key(), zookeeperURL);
        ZookeeperDiscoveryStrategy discovery1 = new ZookeeperDiscoveryStrategy(node,
                loggerFactory.getLogger("discovery"), props);
        ZookeeperDiscoveryStrategy discovery2 = new ZookeeperDiscoveryStrategy(null,
                loggerFactory.getLogger("discovery"), props);

        discovery1.start(); // registry Node. node is not null -> isMember
        discovery2.start(); // only start curator. NOT registry Node.

        Iterable<DiscoveryNode> nodes = discovery2.discoverNodes();
        System.out.println("Nodes: " + formatNodes(nodes));

        restart();

        nodes = discovery2.discoverNodes();
        System.out.println("Nodes after restart: " + formatNodes(nodes));

        TimeUnit.SECONDS.sleep(50);
        nodes = discovery2.discoverNodes();
        System.out.println("Final Nodes: " + formatNodes(nodes));
        zkTestServer.close();
    }

    private static @NotNull List<Address> formatNodes(Iterable<DiscoveryNode> nodes) {
        return StreamSupport.stream(nodes.spliterator(), false)
                .map(DiscoveryNode::getPrivateAddress).toList();
    }

    private static void restart() throws Exception {
        TimeUnit.SECONDS.sleep(5);
        zkTestServer.restart();
        TimeUnit.SECONDS.sleep(5);
    }

    @Ignore
    @Test
    public void runObserver() {
        Map<String, Comparable> props = Map.of(ZookeeperDiscoveryProperties.ZOOKEEPER_URL.key(), LOCAL_ZK);
        ZookeeperDiscoveryStrategy discovery = new ZookeeperDiscoveryStrategy(null,
                loggerFactory.getLogger("discovery"), props);
        discovery.start();
        System.out.println("Started");
        while (true) {
            try {
                System.out.println("Nodes: " + formatNodes(discovery.discoverNodes()));
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }

    public void runRegistry() throws UnknownHostException {
        DiscoveryNode node = new SimpleDiscoveryNode(new Address("localhost", 123));
        Map<String, Comparable> props = Map.of(ZookeeperDiscoveryProperties.ZOOKEEPER_URL.key(), LOCAL_ZK);
        ZookeeperDiscoveryStrategy discovery = new ZookeeperDiscoveryStrategy(node,
                loggerFactory.getLogger("discovery"), props);
        discovery.start();
        System.out.println("Started");
        while (true) {
            try {
                System.out.println("Nodes: " + formatNodes(discovery.discoverNodes()));
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
}

//[zk: localhost:2181(CONNECTED) 4] get /discovery/hazelcast/hazelcast/4254b919-e5d4-4ffb-b296-7e014e0bd029
//{"name":"hazelcast","id":"4254b919-e5d4-4ffb-b296-7e014e0bd029","address":"localhost","port":123,"sslPort":null,"payload":null,"registrationTimeUTC":1769292890155,"serviceType":"DYNAMIC","uriSpec":{"parts":[{"value":"scheme","variable":true},{"value":"://","variable":false},{"value":"address","variable":true},{"value":":","variable":false},{"value":"port","variable":true}]}}
//
//[zk: localhost:2181(CONNECTED) 6] get /discovery/hazelcast/hazelcast/4254b919-e5d4-4ffb-b296-7e014e0bd029
//{"name":"hazelcast","id":"4254b919-e5d4-4ffb-b296-7e014e0bd029","address":"localhost","port":123,"sslPort":null,"payload":null,"registrationTimeUTC":1769292890155,"serviceType":"DYNAMIC","uriSpec":{"parts":[{"value":"scheme","variable":true},{"value":"://","variable":false},{"value":"address","variable":true},{"value":":","variable":false},{"value":"port","variable":true}]}}
//
//[zk: localhost:2181(CONNECTED) 10] get /discovery/hazelcast/hazelcast/4254b919-e5d4-4ffb-b296-7e014e0bd029
//{"name":"hazelcast","id":"4254b919-e5d4-4ffb-b296-7e014e0bd029","address":"localhost","port":123,"sslPort":null,"payload":null,"registrationTimeUTC":1769292890155,"serviceType":"DYNAMIC","uriSpec":{"parts":[{"value":"scheme","variable":true},{"value":"://","variable":false},{"value":"address","variable":true},{"value":":","variable":false},{"value":"port","variable":true}]}}

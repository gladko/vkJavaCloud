package com.hazelcast.eureka.one;

import com.hazelcast.cluster.Address;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.discovery.EurekaClient;

import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;


public class HazelcastEurekaSandbox {
    static EurekaClient eurekaClient;
    static EurekaOneDiscoveryStrategy strategy;
    static ApplicationInfoManager applicationInfoManager;


    public static void main(String[] args) throws Exception {

//        EurekaClient eurekaClient = ...

        initHazelcastStrategy();
        strategy.start(); // registerNode

        discoverNodes();
        TimeUnit.SECONDS.sleep(10);

        strategy.destroy(); // deregister

        System.out.println("done");
    }

    private static void discoverNodes() {
        Iterable<DiscoveryNode> nodes = strategy.discoverNodes();
        System.out.println(nodes);
    }

    private static void initHazelcastStrategy() throws UnknownHostException {
        DiscoveryNode node = new SimpleDiscoveryNode(new Address("localhost", 123));
        strategy = new EurekaOneDiscoveryStrategy.EurekaOneDiscoveryStrategyBuilder()
//            .setEurekaClient(eurekaClient)
//            .setApplicationInfoManager(applicationInfoManager)
            .setDiscoveryNode(node)
            .setStatusChangeStrategy(new DefaultUpdater())
            .build();
    }
}
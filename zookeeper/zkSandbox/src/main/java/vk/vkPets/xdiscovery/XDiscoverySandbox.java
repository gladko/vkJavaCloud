package vk.vkPets.xdiscovery;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RandomStrategy;
import org.apache.zookeeper.KeeperException;


public class XDiscoverySandbox {
    static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    public static final String PATH = "/discovery/example";

    public static void main(String[] args) throws Exception {
        // This method is scaffolding to get the example up and running

        CuratorFramework client = null;
        ServiceDiscovery<NodeDetails> serviceDiscovery = null;
        Map<String, ServiceProvider<NodeDetails>> providers = new HashMap<>();
        try {
            client = CuratorFrameworkFactory.newClient(ZOOKEEPER_ADDRESS,
                    new ExponentialBackoffRetry(1000, 3));
            client.start();

            JsonInstanceSerializer<NodeDetails> serializer =
                    new JsonInstanceSerializer<>(NodeDetails.class);
            serviceDiscovery = ServiceDiscoveryBuilder.builder(NodeDetails.class)
                    .client(client)
                    .basePath(PATH)
                    .serializer(serializer)
                    .build();
            serviceDiscovery.start();

            processCommands(serviceDiscovery, providers, client);
        } finally {
            for (ServiceProvider<NodeDetails> cache : providers.values()) {
                CloseableUtils.closeQuietly(cache);
            }

            CloseableUtils.closeQuietly(serviceDiscovery);
            CloseableUtils.closeQuietly(client);
        }
    }

    private static void processCommands(
            ServiceDiscovery<NodeDetails> serviceDiscovery,
            Map<String, ServiceProvider<NodeDetails>> providers,
            CuratorFramework client)
            throws Exception
    {
        // More scaffolding that does a simple command line processor

        printHelp();

        List<XServerNode> servers = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            boolean done = false;
            while (!done) {
                System.out.print("> ");

                String line = in.readLine();
                if (line == null) {
                    break;
                }

                String command = line.trim();
                String[] parts = command.split("\\s");
                if (parts.length == 0) {
                    continue;
                }
                String operation = parts[0];
                String[] args = Arrays.copyOfRange(parts, 1, parts.length);

                if (operation.equalsIgnoreCase("help") || operation.equalsIgnoreCase("?")) {
                    printHelp();
                } else if (operation.equalsIgnoreCase("q") || operation.equalsIgnoreCase("quit")) {
                    done = true;
                } else if (operation.equals("add")) {
                    addInstance(args, command, servers);
                } else if (operation.equals("delete")) {
                    deleteInstance(args, command, servers);
                } else if (operation.equals("random")) {
                    listRandomInstance(args, serviceDiscovery, providers, command);
                } else if (operation.equals("list")) {
                    listInstances(serviceDiscovery);
                }
            }
        } finally {
            for (XServerNode server : servers) {
                CloseableUtils.closeQuietly(server);
            }
        }
    }

    private static void listRandomInstance(
            String[] args,
            ServiceDiscovery<NodeDetails> serviceDiscovery,
            Map<String, ServiceProvider<NodeDetails>> providers,
            String command)
            throws Exception
    {
        // this shows how to use a ServiceProvider
        // in a real application you'd create the ServiceProvider early for the service(s) you're interested in

        if (args.length != 1) {
            System.err.println("syntax error (expected random <name>): " + command);
            return;
        }

        String serviceName = args[0];
        ServiceProvider<NodeDetails> provider = providers.get(serviceName);
        if (provider == null) {
            provider = serviceDiscovery
                    .serviceProviderBuilder()
                    .serviceName(serviceName)
                    .providerStrategy(new RandomStrategy<>())
                    .build();
            providers.put(serviceName, provider);
            provider.start();

            Thread.sleep(2500); // give the provider time to warm up - in a real application you wouldn't need to do this
        }

        ServiceInstance<NodeDetails> instance = provider.getInstance();
        if (instance == null) {
            System.err.println("No instances named: " + serviceName);
        } else {
            outputInstance(instance);
        }
    }

    private static void listInstances(ServiceDiscovery<NodeDetails> serviceDiscovery) throws Exception {
        // This shows how to query all the instances in service discovery

        try {
            Collection<String> serviceNames = serviceDiscovery.queryForNames();
            System.out.println(serviceNames.size() + " type(s)");
            for (String serviceName : serviceNames) {
                Collection<ServiceInstance<NodeDetails>> instances =
                        serviceDiscovery.queryForInstances(serviceName);
                System.out.println(serviceName);
                for (ServiceInstance<NodeDetails> instance : instances) {
                    outputInstance(instance);
                }
            }

        } catch (KeeperException.NoNodeException e) {
            System.err.println("There are no registered instances.");
        } finally {
            CloseableUtils.closeQuietly(serviceDiscovery);
        }
    }


    private static void outputInstance(ServiceInstance<NodeDetails> instance) {
        System.out.println("\t" + instance.getPayload().getDescription() + ": " + instance.buildUriSpec());
    }

    private static void deleteInstance(String[] args, String command, List<XServerNode> servers) {
        // simulate a random instance going down
        // in a real application, this would occur due to normal operation, a crash, maintenance, etc.

        if (args.length != 1) {
            System.err.println("syntax error (expected delete <name>): " + command);
            return;
        }

        final String serviceName = args[0];

        Optional<XServerNode> server = servers.stream()
                .filter(s ->  s.getThisInstance().getName().endsWith(serviceName))
                .findAny();
        if (server.isEmpty()) {
            System.err.println("No servers found named: " + serviceName);
            return;
        }

        servers.remove(server.get());
        CloseableUtils.closeQuietly(server.get());
        System.out.println("Removed a random instance of: " + serviceName);
    }

    private static void addInstance(String[] args, String command, List<XServerNode> servers)
            throws Exception {
        // simulate a new instance coming up
        // in a real application, this would be a separate process

        if (args.length < 2) {
            System.err.println("syntax error (expected add <name> <description>): " + command);
            return;
        }

        String description = Arrays.stream(args, 1, args.length).collect(Collectors.joining(" "));

        String serviceName = args[0];
        XServerNode server = new XServerNode(PATH, serviceName, description);
        servers.add(server);
        server.start();

        System.out.println(serviceName + " added");
    }

    private static void printHelp() {
        System.out.println(
                "An example of using the ServiceDiscovery APIs. This example is driven by entering commands at the prompt:\n");
        System.out.println("add <name> <description>: Adds a mock service with the given name and description");
        System.out.println("delete <name>: Deletes one of the mock services with the given name");
        System.out.println("list: Lists all the currently registered services");
        System.out.println("random <name>: Lists a random instance of the service with the given name");
        System.out.println("quit: Quit the example");
        System.out.println();
    }
}
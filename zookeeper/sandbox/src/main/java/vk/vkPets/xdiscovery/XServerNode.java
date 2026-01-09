package vk.vkPets.xdiscovery;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.shaded.com.google.common.io.Closeables;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import java.io.Closeable;

import static vk.vkPets.LogUtil.log;


/**
 * This shows a very simplified method of registering an instance with the service discovery. Each individual
 * instance in your distributed set of applications would create an instance of something similar to ExampleServer,
 * start it when the application comes up and close it when the application shuts down.
 */
public class XServerNode implements Closeable {
    private static final String ZOOKEEPER_ADDRESS = XDiscoverySandbox.ZOOKEEPER_ADDRESS;

    private final ServiceDiscovery<NodeDetails> serviceDiscovery;
    private ServiceInstance<NodeDetails> thisInstance;
    private final ServiceInstanceBuilder<NodeDetails> builder;
    private final CuratorFramework client;

    public XServerNode(String path, String serviceName, String description)
            throws Exception {
        // in a real application, you'd have a convention of some kind for the URI layout
        UriSpec uriSpec = new UriSpec("{scheme}://{name}.com:{port}");

        builder = ServiceInstance.<NodeDetails>builder()
                .name(serviceName)
                .payload(new NodeDetails(description))
                .port((int) (65535 * Math.random())) // in a real application, you'd use a common port
                .uriSpec(uriSpec);

        thisInstance = builder.build();

        // if you mark your payload class with @JsonRootName the provided JsonInstanceSerializer will work
        JsonInstanceSerializer<NodeDetails> serializer = new JsonInstanceSerializer<>(NodeDetails.class);

        client = CuratorFrameworkFactory.newClient(ZOOKEEPER_ADDRESS,
                new ExponentialBackoffRetry(1000, 3));
        client.start();

        serviceDiscovery = ServiceDiscoveryBuilder.builder(NodeDetails.class)
                .client(client)
                .basePath(path)
                .serializer(serializer)
                .thisInstance(thisInstance)
                .build();
    }

    public ServiceInstance<NodeDetails> getThisInstance() {
        return thisInstance;
    }

    public void start() throws Exception {
        serviceDiscovery.start();
//        serviceDiscovery.registerService(thisInstance);
    }

    public void update(String desc) throws Exception {
        thisInstance = builder
                .id(thisInstance.getId())
                .payload(new NodeDetails(desc))
                .build();
        serviceDiscovery.updateService(thisInstance);
    }

    @Override
    public void close() {
        closeQuietly(serviceDiscovery);
        closeQuietly(client);
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            Closeables.close(closeable, true);
        } catch (Throwable t) {
            log(t.toString());
        }
    }

    public static void main(String[] args) throws Exception {
        log("starting");
        try (XServerNode serverNode = new XServerNode("/test", "abc", "desc")) {
            serverNode.start();
            log("started");


            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            log(e.toString());
            throw e;
        }
    }
}

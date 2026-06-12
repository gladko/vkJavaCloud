package vk.vkPets.server;

import io.etcd.jetcd.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

public class NodesMain {
    public static final List<URI> ETCD_ENDPOINT = List.of(URI.create("http://localhost:2379"));
    static final List<URI> ETCD_PROXY_ENDPOINT = List.of(URI.create("http://localhost:12379"));

    static Logger logger = LoggerFactory.getLogger("MAIN");


    public static void main(String[] args) throws Exception {
        Node node1 = new Node(ETCD_ENDPOINT);
        node1.join();

        Thread.sleep(3_000);
        Client observerClient = Client.builder().endpoints(NodesMain.ETCD_ENDPOINT).build();
        Observer observer = new Observer(observerClient, logger);

        Node node2 = new Node(ETCD_ENDPOINT);
        node2.join();

//        Node node3 = new Node(ETCD_PROXY_ENDPOINT);
        Node node3 = new Node(ETCD_ENDPOINT);
        node3.join();

        Thread.sleep(10_000);
        logger.info("ClusterMembers: {}", observer.getClusterMembers());

        showMembers();

        Thread.sleep(5_000);
        node1.leave();

        Thread.sleep(3_000);
        logger.info("After node1 close ClusterMembers: {}", observer.getClusterMembers());

        observer.close();
        observerClient.close();
        List.of(node1, node2, node3).forEach(Node::close);
    }

    private static void showMembers() throws Exception {
        Client etcdClient = Client.builder().endpoints(ETCD_ENDPOINT).build();
        Observer observer = new Observer(etcdClient, logger);
        observer.close();
        etcdClient.close();
    }
}

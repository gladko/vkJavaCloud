package vk.vkPets.server;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class NodesMain {
    static final List<URI> ETCD_ENDPOINT = List.of(URI.create("http://localhost:2379"));

    static Logger logger = LoggerFactory.getLogger("MAIN");


    public static void main(String[] args) throws Exception {
        Node node1 = new Node(ETCD_ENDPOINT);
        node1.join();

        Thread.sleep(3_000);
        Observer observer = new Observer(logger);

        Node node2 = new Node(ETCD_ENDPOINT);
        node2.join();

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
        List.of(node1, node2, node3).forEach(Node::close);
    }

    private static void showMembers() throws Exception {
        Client etcdClient = Client.builder().endpoints(ETCD_ENDPOINT).build();

        GetResponse response = etcdClient.getKVClient().get(
                ByteSequence.from(Node.NODES_PREFIX, StandardCharsets.UTF_8),
                GetOption.builder()
                        .isPrefix(true)
                        .build()
        ).get();

        for (KeyValue kv : response.getKvs()) {
            NodeData nodeData = JsonObjectMapper.read(kv.getValue().toString(StandardCharsets.UTF_8));
            logger.info("---> {}", nodeData);
        }
        etcdClient.close();
    }
}

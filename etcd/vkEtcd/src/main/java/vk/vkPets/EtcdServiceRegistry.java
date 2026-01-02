package vk.vkPets;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public class EtcdServiceRegistry {
    private static final String ETCD_ENDPOINT = "http://localhost:2379";

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // Initialize the etcd client
        Client client = Client.builder().endpoints(ETCD_ENDPOINT).build();

        try (KV kvClient = client.getKVClient()) {
            // Register a service
            String serviceName = "my-service";
            String serviceAddress = "http://localhost:8080";

            ByteSequence key = ByteSequence.from(serviceName, StandardCharsets.UTF_8);
            ByteSequence value = ByteSequence.from(serviceAddress, StandardCharsets.UTF_8);

            kvClient.put(key, value).get();
            System.out.println("Service registered: " + serviceName + " -> " + serviceAddress);

            // Discover a service
            GetResponse getResponse = kvClient.get(key).get();
            if (!getResponse.getKvs().isEmpty()) {
                String foundServiceAddress = getResponse.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
                System.out.println("Discovered service: " + serviceName + " -> " + foundServiceAddress);
            } else {
                System.out.println("Service not found: " + serviceName);
            }
        } finally {
            client.close();
        }
    }

}

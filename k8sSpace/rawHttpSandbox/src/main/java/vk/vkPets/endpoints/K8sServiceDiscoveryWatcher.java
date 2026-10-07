package vk.vkPets.endpoints;


import com.fasterxml.jackson.databind.ObjectMapper;
import vk.vkPets.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Run this class and then
 * kubectl create deployment sample-worker --image=nginx --replicas=2
 * kubectl expose deployment sample-worker --port=80 --target-port=80
 * kubectl scale deployment sample-worker --replicas=1
 */
public class K8sServiceDiscoveryWatcher {
    private static final int RECONNECT_DELAY_MS = 5000; // 5 seconds recovery backoff

    public static void main(String[] args) throws Exception {
        // 1. Point to the core k8s API resource endpoint collection stream
        String k3sServerUrl = "https://127.0.0.1:6443";
        String watchUrl = k3sServerUrl + "/api/v1/namespaces/default/endpoints?watch=true";


        HttpClient client = Util.createHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        System.out.println("📡 Service Discovery Engine Started...");

        // Outer infinite loop keeps the supervisor engine running indefinitely
        while (true) {
            try {
                // Fetch a fresh token on every connection loop in case it expired
                String token = Util.getToken();
                requestEndpoints(watchUrl, token, client, mapper);
            } catch (InterruptedException e) {
                System.out.println("🛑 Watcher interrupted. Shutting down completely.");
                Thread.currentThread().interrupt(); // Restore interrupted status
                break;
            } catch (Exception e) {
                System.err.println("💥 Network disconnection or handshake failure: " + e.getMessage());
            }

            // Recovery phase: Sleep before attempting to re-establish the socket connection
            try {
                System.out.printf("⏳ Waiting %d seconds before attempting to reconnect...%n", RECONNECT_DELAY_MS / 1000);
                Thread.sleep(RECONNECT_DELAY_MS);
            } catch (InterruptedException ie) {
                System.out.println("🛑 Interrupted during reconnect backoff. Shutting down.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private static void requestEndpoints(String watchUrl, String token, HttpClient client, ObjectMapper mapper)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(watchUrl))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        System.out.println("📡 Service Discovery: Listening to active Kubernetes Endpoints...");

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() == 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                String line;

                // Loops indefinitely as long as the cluster streams updates
                while ((line = reader.readLine()) != null) {
                    try {
                        System.out.println("new line" + line);
                        // Map the streamed envelope line straight to our discovery model
                        K8sEndpointEvent event = mapper.readValue(line, K8sEndpointEvent.class);
                        processDiscoveryEvent(event);
                    } catch (Exception e) {
                        System.err.println("⚠ Failed to parse stream line: " + e.getMessage());
                    }
                }
            }

            System.out.println("Stream was terminated or rotated by the API server.");
        } else {
            throw new RuntimeException("Kubernetes API rejected stream connection. HTTP Status: " + response.statusCode());
        }
    }

    /**
     * Extracts live networking topology changes from the stream payload.
     */
    private static void processDiscoveryEvent(K8sEndpointEvent event) {
        // 🛡️ Safe check if k8s sends an unmapped metadata block
        if (event.object() == null || event.object().metadata() == null) return;

        String serviceName = event.object().metadata().name();
        String eventType = event.type();

        // Skip default cluster internal endpoints to keep logs clean
        if ("kubernetes".equals(serviceName)) {
            System.out.println("ignored internal endpoints");
            return;
        }

        System.out.printf("[%s] Service Discovery Event for: '%s'%n", eventType, serviceName);

        if ("DELETED".equals(eventType) || event.object().subsets() == null) {
            System.out.printf("   ❌ Service '%s' went offline completely.%n", serviceName);
            return;
        }

        List<String> liveIps = new java.util.ArrayList<>();
        List<Integer> livePorts = new java.util.ArrayList<>();

        // Map properties cleanly using record components
        for (K8sEndpointEvent.SubSet subset : event.object().subsets()) {
            if (subset.addresses() != null) {
                for (K8sEndpointEvent.Address addr : subset.addresses()) {
                    liveIps.add(addr.ip());
                }
            }
            if (subset.ports() != null) {
                for (K8sEndpointEvent.Port port : subset.ports()) {
                    livePorts.add(port.port());
                }
            }
        }

        if (!liveIps.isEmpty()) {
            System.out.println("   🟢 Available Backend Targets (IP addresses):");
            for (String ip : liveIps) {
                for (int port : livePorts) {
                    System.out.printf("      └─► http://%s:%d%n", ip, port);
                }
            }
        } else {
            System.out.println("   ⚠ No Pods are healthy/ready to receive traffic for this service.");
        }
    }

}

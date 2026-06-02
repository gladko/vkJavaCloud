package vk.vkPets;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.credentials.AccessTokenAuthentication;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;


public class K8sSandbox {
    // todo: remove it! there must be another way to auth the client.
    //   see certificates in /etc/rancher/k3s/k3s.yaml
    // created via 'kubectl -n kubernetes-dashboard create token admin-user --duration=24h'
    // check via 'curl -k -H "Authorization: Bearer $TOKEN" https://127.0.0.1:6443/api'
    private static final String TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjUzYmpXMW9nbmZPUVRYRE04ZUlna2FTSnYyQUxBeVpUeGpOMzhSVjBXeWsifQ.eyJhdWQiOlsiaHR0cHM6Ly9rdWJlcm5ldGVzLmRlZmF1bHQuc3ZjLmNsdXN0ZXIubG9jYWwiLCJrM3MiXSwiZXhwIjoxNzgwMDc2MDU0LCJpYXQiOjE3Nzk5ODk2NTQsImlzcyI6Imh0dHBzOi8va3ViZXJuZXRlcy5kZWZhdWx0LnN2Yy5jbHVzdGVyLmxvY2FsIiwianRpIjoiNmJhM2FjODUtMDNkOS00NmQ0LWJjMzMtMjE2NTQ0NTY0NTQzIiwia3ViZXJuZXRlcy5pbyI6eyJuYW1lc3BhY2UiOiJrdWJlcm5ldGVzLWRhc2hib2FyZCIsInNlcnZpY2VhY2NvdW50Ijp7Im5hbWUiOiJhZG1pbi11c2VyIiwidWlkIjoiMTgyNDY3ZjktMDc4ZS00NjkzLTg4ODEtZDYzYWI3MjA0MTk5In19LCJuYmYiOjE3Nzk5ODk2NTQsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlcm5ldGVzLWRhc2hib2FyZDphZG1pbi11c2VyIn0.NFK56pyjWFxtFy3V9whPLeRbZssXAu6l6ZV8yfHIOM03QQrkBnf6eIXCvX6RbShsd5-bPlNF-IrRL4S4mkJQaTDpFS3ZoJGhcms8rQGMkXSQwXfKhFC_HEj7tEoNE6mrHkfKbwFoMU1UUWAnyEF5IJgaZ9iB5EVwFaHEOBpsqV3vcyT7NV_XMwQsOwpuRAseeb_9N5VfSkiZxn0JQfHovZDVa15QYZNUftu3lJHnplwv9r59uZmLPcdA2t-fLuiQxYO5e_75Kt2NuKB299cwYojJzr7VA4xCC4ifK6fWBarU51DRcgRMv6IAuoeYioYQ2x4q0JAMeeiOc2YRclFIrw";

    private static CoreV1Api api;

    public static void main(String[] args) throws IOException, ApiException {
        api = initApiClient();

        System.out.println(api.listNamespace().execute());

        // Fetch the service metadata
        serviceMetadata(api);

        podInfo(api);

        podsOfService("k8s-hello-service", "default");
    }

    private static @NotNull CoreV1Api initApiClient() throws IOException {
        // Initialize API client
//        ApiClient client = new ApiClient();
//        client.setBasePath("https://127.0.0.1:6443");
//        client.setAccessToken(setBearerToken("<YOUR_TOKEN>");
//        client.setVerifyingSsl(false); // or load CA cert properly

        ApiClient client = ClientBuilder.standard()
                .setBasePath("https://127.0.0.1:6443")
                .setAuthentication(new AccessTokenAuthentication(TOKEN))
                .setVerifyingSsl(false)
                .build();

        Configuration.setDefaultApiClient(client);
        return new CoreV1Api();
    }

    private static void podsOfService(String serviceName, String namespace) throws ApiException {
        // 1. Read the Service
        V1Service svc = api.readNamespacedService(serviceName, namespace).execute();

        // 2. Extract selector labels
        Map<String, String> selector = svc.getSpec().getSelector();

        if (selector == null || selector.isEmpty()) {
            System.out.println("Service has no selector.");
            return;
        }

        // 3. Convert selector map → label selector string
        String labelSelector = selector.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(","));

        System.out.println("Label selector: " + labelSelector);

        // 4. List pods matching the selector
        V1PodList pods = api.listNamespacedPod(namespace)
                .labelSelector(labelSelector)
                .execute();

        pods.getItems().forEach(p ->
                System.out.println("Pod: " + p.getMetadata().getName()
                        + ", ip=" + p.getStatus().getPodIP())
        );
    }

    private static void podInfo(CoreV1Api api) throws ApiException {
        V1Pod pod = api.readNamespacedPod("vk-cloud-hello-64655f54cf-4bsbn", "default").execute();
        System.out.println(pod);
        System.out.println(pod.getSpec());
    }

    private static void serviceMetadata(CoreV1Api api) throws ApiException {
        V1Service service = api.readNamespacedService("k8s-hello-service", "default").execute();

        // Print out labels and annotations
        V1ObjectMeta metadata = service.getMetadata();
        if (metadata != null) {
            System.out.println("Labels: " + metadata.getLabels());
            System.out.println("Annotations: " + metadata.getAnnotations());
        } else {
            System.out.println("metadata is null !!!");
        }
    }
}
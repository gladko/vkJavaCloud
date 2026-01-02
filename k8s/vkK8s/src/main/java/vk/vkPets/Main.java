package vk.vkPets;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.ClientBuilder;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ApiException {
        // Initialize API client
        ApiClient client = ClientBuilder.standard().build();
        Configuration.setDefaultApiClient(client);

        // Initialize CoreV1Api
        CoreV1Api api = new CoreV1Api();

        // Fetch the service metadata
        V1Service service = api.readNamespacedService("my-service", "default", null);

        // Print out labels and annotations
        System.out.println("Labels: " + service.getMetadata().getLabels());
        System.out.println("Annotations: " + service.getMetadata().getAnnotations());
        System.out.println("Hello world!");
    }
}
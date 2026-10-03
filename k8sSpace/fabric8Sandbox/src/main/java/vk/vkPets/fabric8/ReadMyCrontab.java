package vk.vkPets.fabric8;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class ReadMyCrontab {
    private static final String MY_CUSTOM_RESOURCE_NAME = "my-new-cron-object";

    public static void main(String[] args) throws IOException {
        Config config = Config.fromKubeconfig(
//                Files.readString(Path.of("/etc/rancher/k3s/k3s.yaml")));  //
//                Files.readString(Path.of("~/.kube/config")));
                Files.readString(Path.of("/home/vk/.kube/config")));

        try (KubernetesClient client = new KubernetesClientBuilder()
//                .withConfig(config)
                .build()) {
            typedRead(client);
            genericRead(client);
        }
    }

    private static void typedRead(KubernetesClient client) {
        CronTab widget =  client.resources(CronTab.class)
                .inNamespace("default")
                .withName(MY_CUSTOM_RESOURCE_NAME)
                .get();

        if (widget != null) {
            System.out.println("Name: " + widget.getMetadata().getName());
            System.out.println("Size: " + widget.getSpec().getCronStmt());
        }
    }

    private static void genericRead(KubernetesClient client) {
        GenericKubernetesResource resource = client.genericKubernetesResources(
                                    "stable.example.com/v1",
                                "CronTab")
                        .inNamespace("default")
                        .withName(MY_CUSTOM_RESOURCE_NAME)
                        .get();

        System.out.println(resource.getAdditionalProperties());
    }
}
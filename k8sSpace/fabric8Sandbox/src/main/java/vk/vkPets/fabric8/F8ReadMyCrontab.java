package vk.vkPets.fabric8;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;

import java.io.IOException;
import java.util.Map;


public class F8ReadMyCrontab {
    static final String CRONTAB_CR_NAME = "crontabs";
    static final String CRONTAB_CR_API_VERSION = "stable.example.com/v1";
    private static final String MY_CUSTOM_RESOURCE_NAME = "nightly-backup-cron";

    public static void main(String[] args) throws IOException {

        // 1. Initialize client. Fabric8 automatically tracks down your ~/.kube/config
        try (KubernetesClient client = new KubernetesClientBuilder().build()) {
            typedRead(client);
            genericRead(client);
        } catch (KubernetesClientException e) {
            System.err.println("❌ K8s Communication Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void typedRead(KubernetesClient client) {
        System.out.println("=== Fetching CronTab via Fabric8 Typed Client ===");

        CronTab cronTab =  client.resources(CronTab.class)
                .inNamespace("default")
                .withName(MY_CUSTOM_RESOURCE_NAME)
                .get();

        if (cronTab == null) {
            System.err.println("❌ Error: The requested CronTab instance could not be found.");
            return;
        }

        printMetadata(cronTab.getMetadata());

        // Extract values directly through compiler-checked getter methods!
        CronTabSpec spec = cronTab.getSpec();

        if (spec != null) {
            System.out.printf("⚙️  Schedule:  %s%n", spec.getCronStmt());
            System.out.printf("🐳 Icon: %s%n", spec.getIconPath());
            System.out.printf("👥 Replicas:  %d%n", spec.getReplicas());
        } else {
            System.err.println("⚠ Warning: Spec block was empty.");
        }
        System.out.println("==================================================");
    }

    private static void genericRead(KubernetesClient client) {
        System.out.println("=== Fetching CronTab via Fabric8 Dynamic Client ===");

        GenericKubernetesResource cronTab = client.genericKubernetesResources(CRONTAB_CR_API_VERSION, "CronTab")
                .inNamespace("default")
                .withName(MY_CUSTOM_RESOURCE_NAME)
                .get();

        if (cronTab == null) {
            System.err.println("❌ Error: The requested CronTab instance could not be found.");
            return;
        }

        printMetadata(cronTab.getMetadata());

        System.out.println("\n--- RAW PRETTY PRINTED JSON ---");
        // fails in fabric8 version 7.9.0.
//        System.out.println(Serialization.asJson(cronTab));
//        System.out.println(client.getKubernetesSerialization().asJson(cronTab));

        // Fabric8 extracts custom content directly into a Java Map structure
        Map<String, Object> spec = cronTab.get("spec");
        if (spec != null) {
            String cronSpec = (String) spec.getOrDefault("cronStmt", "N/A");
            String image    = (String) spec.getOrDefault("iconPath", "N/A");
            int replicas    = (Integer) spec.getOrDefault("replicas", 1);

            System.out.printf("⚙️  Schedule:  %s%n", cronSpec);
            System.out.printf("🐳 Icon: %s%n", image);
            System.out.printf("🐳 replicas: %s%n", replicas);
        }
        System.out.println("==================================================");
    }

    private static void printMetadata(ObjectMeta objectMeta) {
        System.out.println("\n==================================================");
        System.out.println("📦 KUBERNETES CUSTOM RESOURCE FOUND");
        System.out.println("==================================================");
        System.out.printf("🔹 Namespace: %s%n", objectMeta.getNamespace());
        System.out.printf("🔹 CR Name:   %s%n", objectMeta.getName());
        System.out.println("--------------------------------------------------");
    }
}
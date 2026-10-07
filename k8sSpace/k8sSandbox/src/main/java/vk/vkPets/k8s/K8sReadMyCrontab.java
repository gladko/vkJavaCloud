package vk.vkPets.k8s;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Config;


public class K8sReadMyCrontab {
    // see crontabs_crd.yaml
    static final String CRONTAB_CR_NAME = "crontabs";
    static final String CRONTAB_CR_GROUP = "stable.example.com";
    static final String CRONTAB_CR_VERSION = "v1";


    public static void main(String[] args) throws Exception {
        ApiClient client = Config.fromConfig("/home/vk/.kube/config");
//                Config.fromConfig("/etc/rancher/k3s/k3s.yaml");    //  permission denied

        CustomObjectsApi api = new CustomObjectsApi(client);

        Object rawResponse  = api.getNamespacedCustomObject(
                CRONTAB_CR_GROUP,
                CRONTAB_CR_VERSION,
                "default",   // namespace
                CRONTAB_CR_NAME,
                "nightly-backup-cron"  // instance name
        ).execute();

        System.out.println("my crontabs: " + rawResponse);

        Gson gson = new Gson();
        JsonObject crJson = gson.toJsonTree(rawResponse).getAsJsonObject();

        System.out.println("crJson: " + crJson);

        // 5. Safely navigate and print clean, formatted fields
        printCleanFormat(crJson);
    }


    /**
     * Helper to drill into the JSON tree and display values cleanly.
     */
    private static void printCleanFormat(JsonObject crJson) {
        // Extract metadata attributes
        JsonObject metadata = crJson.getAsJsonObject("metadata");
        String name = metadata != null && metadata.has("name")
                ? metadata.get("name").getAsString() : "Unknown";
        String namespace = metadata != null && metadata.has("namespace")
                ? metadata.get("namespace").getAsString() : "Unknown";

        // Extract custom CRD specification block fields
        JsonObject spec = crJson.getAsJsonObject("spec");
        String cronStmt = spec != null && spec.has("cronStmt")
                ? spec.get("cronStmt").getAsString() : "N/A";
        String icon    = spec != null && spec.has("iconPath")
                ? spec.get("iconPath").getAsString()    : "N/A";
        int replicas    = spec != null && spec.has("replicas")
                ? spec.get("replicas").getAsInt()    : 1;

        // Print cleanly formatted output
        System.out.println("==================================================");
        System.out.println("📦 KUBERNETES CUSTOM RESOURCE FOUND");
        System.out.println("==================================================");
        System.out.printf("🔹 Namespace: %s%n", namespace);
        System.out.printf("🔹 CR Name:   %s%n", name);
        System.out.println("--------------------------------------------------");
        System.out.printf("⚙️  Schedule:  %s%n", cronStmt);
        System.out.printf("🐳 Icon:  %s%n", icon);
        System.out.printf("👥 Replicas:  %d%n", replicas);
        System.out.println("==================================================");
    }

}
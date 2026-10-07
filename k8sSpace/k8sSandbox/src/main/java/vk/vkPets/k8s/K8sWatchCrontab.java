package vk.vkPets.k8s;

import com.google.gson.JsonObject;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.generic.KubernetesApiResponse;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesApi;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesListObject;

import static vk.vkPets.k8s.K8sReadMyCrontab.*;

public class K8sWatchCrontab {

    public static void main(String[] args) throws Exception {
        // 1. Initialize API Client from local kubeconfig
        ApiClient client = Config.fromConfig("/home/vk/.kube/config");

        // 2. Build a GenericClient for our unstructured CRD type
//        GenericKubernetesApi<DynamicKubernetesObject, DynamicKubernetesListObject> cronTabApi =
//                new GenericKubernetesApi<>(
//                        DynamicKubernetesObject.class,
//                        DynamicKubernetesListObject.class,
//                        "stable.example.com", // API Group
//                        "v1",                 // API Version
//                        "crontabs",           // Resource Plural
//                        client
//                );

        DynamicKubernetesApi cronTabApi = new DynamicKubernetesApi(
                CRONTAB_CR_GROUP,   // API Group
                CRONTAB_CR_VERSION, // API Version
                CRONTAB_CR_NAME,    // Resource Plural
                client              // ApiClient
        );


        // 3. Clean Printout: List existing instances in the "default" namespace
        System.out.println("=== Fetching existing CronTabs ===");
        KubernetesApiResponse<DynamicKubernetesListObject> response = cronTabApi.list("default");

        if (response.isSuccess() && response.getObject() != null) {
            for (DynamicKubernetesObject cr : response.getObject().getItems()) {
                printCleanCronTab(cr);
            }
        } else {
            System.err.println("Failed to fetch CronTabs: " +
                    (response.getStatus() != null ? response.getStatus().getMessage() : "Unknown Error"));
        }

        // 4. Watch for live updates using an Informer
        System.out.println("\n=== Starting Live Watcher (Press Ctrl+C to exit) ===");
        SharedInformerFactory factory = new SharedInformerFactory(client);

        // Creates a list-watch loop specifically targeted for our CRD inside the "default" namespace
        SharedIndexInformer<DynamicKubernetesObject> informer =
                factory.sharedIndexInformerFor(cronTabApi, DynamicKubernetesObject.class, 0, "default");

        // Attach event hooks for Add, Update, and Delete changes
        informer.addEventHandler(new ResourceEventHandler<DynamicKubernetesObject>() {
            @Override
            public void onAdd(DynamicKubernetesObject obj) {
                System.out.print("[🟢 ADDED] ");
                printCleanCronTab(obj);
            }

            @Override
            public void onUpdate(DynamicKubernetesObject oldObj, DynamicKubernetesObject newObj) {
                System.out.print("[🟡 MODIFIED] ");
                printCleanCronTab(newObj);
            }

            @Override
            public void onDelete(DynamicKubernetesObject obj, boolean deletedFinalStateUnknown) {
                System.out.println("[🔴 DELETED] CronTab: " + obj.getMetadata().getName());
            }
        });

        // Start the background listening thread loop
        factory.startAllRegisteredInformers();

        // Keep the application running to listen for events
        Thread.currentThread().join();
    }

    /**
     * Helper method to parse out data fields neatly instead of dumping raw JSON string maps.
     */
    private static void printCleanCronTab(DynamicKubernetesObject cr) {
        V1ObjectMeta metadata = cr.getMetadata();

        // Extract out properties safely from our custom 'spec' block using Gson helper wrappers
        JsonObject spec = cr.getRaw().getAsJsonObject("spec");
        String cronSpec = spec != null && spec.has("cronStmt") ? spec.get("cronStmt").getAsString() : "N/A";
        String image    = spec != null && spec.has("iconPath")  ? spec.get("iconPath").getAsString()    : "N/A";
        int replicas    = spec != null && spec.has("replicas") ? spec.get("replicas").getAsInt()    : 1;

        System.out.printf("Name: %-25s | Schedule: %-10s | Image: %-20s | Replicas: %d%n",
                metadata.getName(), cronSpec, image, replicas);
    }
}

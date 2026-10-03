package vk.vkPets.k8s;


import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Config;


public class K8sReadMyCrontab {

    public static void main(String[] args) throws Exception {

        ApiClient client = Config.fromConfig("/home/vk/.kube/config");
//                Config.fromConfig("/etc/rancher/k3s/k3s.yaml");    //  permission denied

        CustomObjectsApi api = new CustomObjectsApi(client);

        Object cr = api.getNamespacedCustomObject(
                "stable.example.com", // group
                "v1",                 // version
                "default",            // namespace
                "crontabs",           // plural
                "my-new-cron-object"  // name
        );

        System.out.println(cr);
    }
}
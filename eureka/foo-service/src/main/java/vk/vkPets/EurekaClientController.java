package vk.vkPets;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.EurekaEvent;
import com.netflix.discovery.EurekaEventListener;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import org.springframework.cloud.netflix.eureka.EurekaServiceInstance;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EurekaClientController implements EurekaEventListener{
    private final EurekaClient eurekaClient;
    private final EurekaClientConfig clientConfig;

    public EurekaClientController(EurekaClient eurekaClient, EurekaClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.eurekaClient = eurekaClient;

        eurekaClient.registerEventListener(this);
    }

    @Override
    public void onEvent(EurekaEvent event) {
        System.out.println("--> onEvent: " + event);
    }

    @GetMapping("/nodes")
    public List<EurekaServiceInstance> getNodes(@RequestParam String service) {
        List<InstanceInfo> infos = this.eurekaClient.getInstancesByVipAddress(service, false);
        return infos.stream().map(EurekaServiceInstance::new).toList();
    }

    @GetMapping("/services")
    public List<String> getServices() {
        Applications applications = this.eurekaClient.getApplications();
        List<Application> registered = applications.getRegisteredApplications();
        return registered.stream().map(app -> app.getName().toLowerCase()).toList();
    }


//    public static void main(String[] args) throws InterruptedException {
//        EurekaInstanceConfig instanceConfig = new CloudInstanceConfig();
//        InstanceInfo instanceInfo = new InstanceInfoFactory().create(instanceConfig);
//        ApplicationInfoManager applicationInfoManager = new ApplicationInfoManager(instanceConfig, instanceInfo);
//
//        TransportClientFactories transportClientFactories = Jersey3TransportClientFactories.;
//        EurekaClientConfig clientConfig = new DefaultEurekaClientConfig();
//        DiscoveryClient discoveryClient = new DiscoveryClient(applicationInfoManager, clientConfig, transportClientFactories);
//        List<InstanceInfo> nodes = discoveryClient.getInstancesByVipAddress("test", false);
//        System.out.println(nodes);
//
//        Thread.sleep(Long.MAX_VALUE);
//    }
}

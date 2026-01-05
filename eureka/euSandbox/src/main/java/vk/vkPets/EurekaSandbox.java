package vk.vkPets;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.CloudInstanceConfig;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.shared.transport.jersey.TransportClientFactories;
import com.netflix.discovery.shared.transport.jersey3.Jersey3TransportClientFactories;
import java.util.List;

public class EurekaSandbox {
    public static void main(String[] args) throws InterruptedException {
        EurekaInstanceConfig instanceConfig = new CloudInstanceConfig();
//        InstanceInfo instanceInfo = new InstanceInfoFactory().create(instanceConfig);
        InstanceInfo instanceInfo = InstanceInfo.Builder.newBuilder()
//                .TODO
                .build();
        ApplicationInfoManager applicationInfoManager = new ApplicationInfoManager(instanceConfig, instanceInfo);

        EurekaClientConfig clientConfig = new DefaultEurekaClientConfig();
        TransportClientFactories transportClientFactories = Jersey3TransportClientFactories.getInstance();

        DiscoveryClient discoveryClient = new DiscoveryClient(applicationInfoManager, clientConfig, transportClientFactories);


        List<InstanceInfo> nodes = discoveryClient.getInstancesByVipAddress("test", false);
        System.out.println(nodes);

        Thread.sleep(Long.MAX_VALUE);
    }
}
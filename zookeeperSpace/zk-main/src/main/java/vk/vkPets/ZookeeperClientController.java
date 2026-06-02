package vk.vkPets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceInstance;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceWatch;
import org.springframework.cloud.zookeeper.serviceregistry.ServiceInstanceRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ZookeeperClientController {

    private final ZookeeperDiscoveryClient zkDiscoveryClient;
    private final ZookeeperServiceRegistry registry;
    @Autowired
    private ZookeeperServiceWatch watch;

    public ZookeeperClientController(ZookeeperDiscoveryClient zkDiscoveryClient, ZookeeperServiceRegistry registry) {
        this.registry = registry;
        this.zkDiscoveryClient = zkDiscoveryClient;

        register();
//        watch.setApplicationEventPublisher();
    }

    @GetMapping("/nodes")
    public List<org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance>> getNodes(@RequestParam String service) {
        List<org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance>> instances =
                zkDiscoveryClient.getInstances(service).stream()
                  .map(i -> ((ZookeeperServiceInstance) i).getServiceInstance()).toList();
        System.out.println("Discovered: " + instances);
        return instances;
    }

    @GetMapping("/services")
    public List<String> getServices() {
        return zkDiscoveryClient.getServices();
    }

    @PostMapping("/register")
    public String register() {
        ZookeeperRegistration zookeeperRegistration = ServiceInstanceRegistration.builder()
                .name("xxx")
                .address("xxx:123")
                .build();
        registry.register(zookeeperRegistration);
        return "Ok";
    }

}

package vk.vkPets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
public class FooController {
    private final DiscoveryClient discoveryClient;
    private final RestClient restClient;

    @Autowired
    private LoadBalancerClient loadBalancer;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${hello-service-name}")
    private String helloServiceName;

    public FooController(DiscoveryClient discoveryClient, RestClient.Builder restClientBuilder) {
        this.discoveryClient = discoveryClient;
        restClient = restClientBuilder.build();
    }


    @GetMapping("/")
    public String index() {
        return "Greetings from Foo app!" +
                "<br>Try the following endpoints:" +
                "<br>  /foo" +
                "<br>  /testresttemplate" +
                "<br>  /discovery?service=" + helloServiceName +
                "<br>  /lb?service=" + helloServiceName;
    }

    @GetMapping("/testresttemplate")
    public String testRestTemplate() {
        return restTemplate.getForObject("http://" + helloServiceName + "/hi", String.class);
    }

    @GetMapping("/foo")
    public String foo() {
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(helloServiceName);
        System.out.println("Found hello service instances: " + serviceInstances);
        if (serviceInstances.isEmpty()) {
            return "Hello service not found. Register any instance";
        }
        ServiceInstance serviceInstance = serviceInstances.get(0);
        String helloServiceResponse = restClient.get()
                .uri(serviceInstance.getUri() + "/hi")
                .retrieve()
                .body(String.class);

        System.out.println("Received response from hello service: " + helloServiceResponse);
        return helloServiceResponse;
    }

    @GetMapping("/discovery")
    public String discover(@RequestParam String service) {
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(service);
        System.out.println("Discovered: " + serviceInstances);
        return serviceInstances.toString();
    }

    @GetMapping("/lb")
    public ServiceInstance lb(@RequestParam String service) {
        return this.loadBalancer.choose(service);
    }
}
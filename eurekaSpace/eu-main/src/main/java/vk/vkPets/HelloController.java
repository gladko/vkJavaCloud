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
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RestController
public class HelloController {
    private final DiscoveryClient discoveryClient;
    private final RestClient restClient;

    @Autowired
    private LoadBalancerClient loadBalancer;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${translate-service-name}")
    private String translateServiceName;

    public HelloController(DiscoveryClient discoveryClient, RestClient.Builder restClientBuilder) {
        this.discoveryClient = discoveryClient;
        restClient = restClientBuilder.build();
    }


    @GetMapping("/")
    public String index() {
        return "Greetings from Eureka space main app!" +
                "<br>Try the following endpoints:" +
                "<br>  /hi" +
                "<br>  /testresttemplate" +
                "<br>  /discovery?service=" + translateServiceName +
                "<br>  /choose?service=" + translateServiceName +
                "<br>  /nodes?service=" + translateServiceName +
                "<br>  /services";
    }

    @GetMapping("/testresttemplate")
    public String testRestTemplate() {
        return restTemplate.getForObject("http://" + translateServiceName + "/ping", String.class);
    }

    @GetMapping("/hi")
    public String hi() {
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(translateServiceName);
        System.out.println("Found translate service instances: " + serviceInstances);
        if (serviceInstances.isEmpty()) {
            return "translate-service not found. Register any instance";
        }

        int randomInstance = ThreadLocalRandom.current().nextInt(serviceInstances.size());

        ServiceInstance serviceInstance = serviceInstances.get(randomInstance);
        String translateServiceResponse = restClient.get()
                .uri(serviceInstance.getUri() + "/ping")
                .retrieve()
                .body(String.class);

        System.out.println("Received response from translate-service: " + translateServiceResponse);
        return translateServiceResponse;
    }

    @GetMapping("/discovery")
    public String discover(@RequestParam String service) {
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(service);
        System.out.println("Discovered: " + serviceInstances);
        return serviceInstances.stream()
                .map(ServiceInstance::toString)
                .collect(Collectors.joining("<br>"));
    }

    @GetMapping("/choose")
    public ServiceInstance choose(@RequestParam String service) {
        return this.loadBalancer.choose(service);
    }
}
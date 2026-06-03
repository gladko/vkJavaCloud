package vk.vkPets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@EnableDiscoveryClient
@SpringBootApplication
public class SpringK8sMainApp {
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(SpringK8sMainApp.class, args);
    }

    @Bean
    @LoadBalanced  // Enables Ribbon load balancing with Eureka
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

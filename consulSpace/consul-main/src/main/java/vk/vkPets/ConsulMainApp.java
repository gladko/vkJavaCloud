package vk.vkPets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;


@EnableDiscoveryClient
@SpringBootApplication
public class ConsulMainApp {
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(ConsulMainApp.class, args);

        HelloController helloController = ctx.getBean(HelloController.class);
        new Thread(() -> {
            while (true) {
                try {
                    System.out.println(helloController.hi());
                } catch (Exception e) {
                    System.out.println(e);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
//        printBeans(ctx);
    }

    @Bean
    @LoadBalanced  // Enables Ribbon load balancing with Eureka
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    private static void printBeans(ApplicationContext ctx) {
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
    }
}

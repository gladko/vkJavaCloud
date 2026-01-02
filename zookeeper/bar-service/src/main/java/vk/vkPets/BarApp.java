package vk.vkPets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;


@EnableDiscoveryClient
@SpringBootApplication
public class BarApp {
    @Value("${spring.application.name:testZookeeperApp}")
    private String appName;

    @Autowired
    private LoadBalancerClient loadBalancer;

    @Autowired
    private DiscoveryClient discovery;

    @Autowired
    private Environment env;

    @Autowired
    private AppClient appClient;

    @Autowired(required = false)
    private Registration registration;

    private RestTemplate rest;

    @RequestMapping("/")
    public ServiceInstance lb() {
        return this.loadBalancer.choose(this.appName);
    }

    @RequestMapping("/hi")
    public String hi() {
        return "Hello World! from " + this.registration;
    }

    @RequestMapping("/self")
    public String self() {
        return this.appClient.hi();
    }

    @RequestMapping("/myenv")
    public String env(@RequestParam("prop") String prop) {
        return this.env.getProperty(prop, "Not Found");
    }

    public String rt() {
        return this.rest.getForObject("http://" + this.appName + "/hi", String.class);
    }

    @Bean
    @LoadBalanced
    RestTemplate loadBalancedRestTemplate() {
        this.rest = new RestTemplateBuilder().build();
        return this.rest;
    }

    @FeignClient("testZookeeperApp")
    interface AppClient {

        @RequestMapping(path = "/hi", method = RequestMethod.GET)
        String hi();
    }



    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(BarApp.class, args);

        printBeans(ctx);
    }

    private static void printBeans(ApplicationContext ctx) {
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
    }
}

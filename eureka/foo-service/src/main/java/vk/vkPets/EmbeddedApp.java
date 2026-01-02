package vk.vkPets;


/*
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

public class EmbeddedApp {

    @FeignClient("testZookeeperApp")
    interface AppClient {
        @RequestMapping(path = "/hi", method = RequestMethod.GET)
        String hi();
//    }


    @Autowired
    private AppClient appClient;
    @Autowired(required = false)
    private Registration registration;

    @Autowired
    private Environment env;

    @GetMapping("/property")
    public String env(@RequestParam("name") String prop) {
        return this.env.getProperty(prop, "Not Found");
    }

    private RestTemplate rest;


    public String rt(String appName) {
        return this.rest.getForObject("http://" + appName + "/hi", String.class);
    }

    @Bean
    @LoadBalanced
    RestTemplate loadBalancedRestTemplate() {
        this.rest = new RestTemplateBuilder().build();
        return this.rest;
    }

    @RequestMapping("/self")
    public String self() {
        return this.appClient.hi();
    }

    @RequestMapping("/hi")
    public String hi() {
        return "Hello World! from " + this.registration;
    }


}

 */

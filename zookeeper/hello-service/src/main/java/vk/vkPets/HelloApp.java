package vk.vkPets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@EnableDiscoveryClient
@SpringBootApplication
@RestController
public class HelloApp {
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(HelloApp.class, args);

        System.out.println("Let's inspect the beans provided by Spring Boot:");
        String[] beanNames = ctx.getBeanDefinitionNames();
        System.out.println("spring beans length: " + beanNames.length);
//        Arrays.sort(beanNames);
//        for (String beanName : beanNames) {
//            System.out.println(beanName);
//        }
    }

    @Autowired
    private Environment env;

    @GetMapping("/")
    public String index() {
        return "Greetings from Hello-app!" +
                "<br>Try the following endpoints" +
                "<br>  /ping" +
                "<br>  /hi" +
                "<br>  /prop?name=X";
    }

    @GetMapping("/ping")
    public String ping() {
        System.out.println("requested ping");
        return "Pong";
    }

    @GetMapping("/hi")
    public String hi() {
        System.out.println("requested hi");
        return "Guten morgen";
    }

    @RequestMapping("/prop")
    public String env(@RequestParam String name) {
        return this.env.getProperty(name, "Not Found");
    }
}

package vk.vkPets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

@SpringBootApplication
@RestController
public class K8sTranslateApp {
    private static final String INSTANCE_UUID = UUID.randomUUID().toString();
    private static String HOST_NAME;


    public static void main(String[] args) throws UnknownHostException {
        HOST_NAME = InetAddress.getLocalHost().getHostName();
        ApplicationContext ctx = SpringApplication.run(K8sTranslateApp.class, args);

        System.out.println("Let's inspect the beans provided by Spring Boot:");
        String[] beanNames = ctx.getBeanDefinitionNames();
        System.out.println("spring beans length: " + beanNames.length);
    }

    @Autowired
    private Environment env;

    @GetMapping("/")
    public String index() {
        return "Greetings from Translate app!" +
                "<br>Try the following endpoints" +
                "<br>  /ping" +
                "<br>  /prop?name=X";
    }

    @GetMapping("/ping")
    public String ping() throws UnknownHostException {
        System.out.println("requested ping, instance : " + INSTANCE_UUID);
        return "Pong from " + HOST_NAME + ", uuid: " + INSTANCE_UUID;
    }

    @RequestMapping("/prop")
    public String env(@RequestParam String name) {
        return this.env.getProperty(name, "Not Found");
    }
}

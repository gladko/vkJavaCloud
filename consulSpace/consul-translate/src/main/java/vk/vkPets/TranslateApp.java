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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

@EnableDiscoveryClient
@SpringBootApplication
@RestController
public class TranslateApp {
    private static final Map<String, String> DICTIONARY = Map.of(
            "Hello", "Hallo",
            "Good morning", "Guten Morgen",
            "Good day", "Guter Tag",
            "Good evening", "Guten Abend"
    );

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(TranslateApp.class, args);

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
        return "Greetings from Translate app!" +
                "<br>Try the following endpoints" +
                "<br>  /ping" +
                "<br>  /translate" +
                "<br>  /prop?name=X";
    }

    @GetMapping("/ping")
    public String ping() throws UnknownHostException {
        System.out.println("requested ping, host: " + InetAddress.getLocalHost().getHostAddress());
        return "Pong";
    }

    @GetMapping("/translate")
    public String translate(String input) throws Exception {
        System.out.println("requested " + input + ", host: " + InetAddress.getLocalHost().getHostAddress());
        return DICTIONARY.getOrDefault(input, "UNKNOWN");
    }

    @RequestMapping("/prop")
    public String env(@RequestParam String name) {
        return this.env.getProperty(name, "Not Found");
    }
}

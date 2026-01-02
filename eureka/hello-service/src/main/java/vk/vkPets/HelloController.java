package vk.vkPets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
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
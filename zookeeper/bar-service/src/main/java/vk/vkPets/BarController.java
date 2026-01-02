package vk.vkPets;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BarController {

    @GetMapping("/")
    public String index() {
        return "Greetings from Bar app!";
    }

}
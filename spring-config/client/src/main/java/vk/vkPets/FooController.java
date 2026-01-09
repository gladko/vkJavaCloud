package vk.vkPets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class FooController {
    @Value("${info.foo}")
    private String foo;
    @Autowired
    private Environment env;

    @RequestMapping("/foo")
    public String foo() {
        return foo;
    }

    @RequestMapping("/prop")
    public String env(@RequestParam String name) {
        return this.env.getProperty(name, "Not Found");
    }
}

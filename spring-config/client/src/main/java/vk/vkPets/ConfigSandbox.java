package vk.vkPets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

@SpringBootApplication
public class ConfigSandbox {

    public static void main(String[] args) {
        try {
            ApplicationContext ctx = SpringApplication.run(ConfigSandbox.class, args);
            System.out.println(ctx);

            System.out.println("Let's inspect the beans provided by Spring Boot:");
            String[] beanNames = ctx.getBeanDefinitionNames();
            System.out.println("spring beans length: " + beanNames.length);
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }
        } catch (Throwable t) {
            System.out.println(t);
        }
    }

}
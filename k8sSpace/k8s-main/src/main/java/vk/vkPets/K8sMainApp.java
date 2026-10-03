package vk.vkPets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;


@SpringBootApplication
public class K8sMainApp {
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(K8sMainApp.class, args);

//        printBeans(ctx);
    }

    @Bean
    RestClient restClient() {
        return RestClient.builder().build();
    }
}

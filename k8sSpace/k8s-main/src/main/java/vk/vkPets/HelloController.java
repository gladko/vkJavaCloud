package vk.vkPets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
public class HelloController {

    private final RestClient client;
    private String translateServiceAddress;
    @Value("${spring.application.name}")
    private String spaceName;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();


    public HelloController(RestClient client,
            @Value("${translate-service-address}") String translateServiceAddress)
    {
        this.client = client;
        this.translateServiceAddress = translateServiceAddress;
    }


    @GetMapping("/")
    public String index() {
        return "Greetings from " + spaceName + " space main app!" +
                "<br>Try the following endpoints:" +
                "<br>  /testCall" +
                "<br>  /go";
    }

    @GetMapping("/testCall")
    public String testCall() {
        System.out.println("on testCall");
//        return restTemplate.getForObject(translateServiceAddress + "/ping", String.class);
        return client.get()
                .uri(translateServiceAddress + "/ping")
                .retrieve()
                .body(String.class);
    }

    @GetMapping("/go")
    public String go() {
        executorService.scheduleAtFixedRate(() -> {
            try {
                String result = testCall();
                System.out.println(new Date().toString() + ": " + result);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, 1, 1, TimeUnit.SECONDS);

        return "ok";
    }
}
package vk.vkPets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class HelloController {
    private final Map<String, Integer> results = new ConcurrentHashMap<>();
    private final AtomicInteger requestCounter = new AtomicInteger();
    private final AtomicBoolean logRequestResults = new AtomicBoolean();

    private String translateServiceAddress;
    @Value("${spring.application.name}")
    private String spaceName;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final RestClient restClient;
    private final GreetingsService grpsClient;

    public HelloController(RestClient restClient, GreetingsService grpsClient,
            @Value("${translate-service-address}") String translateServiceAddress)
    {
        this.restClient = restClient;
        this.grpsClient = grpsClient;

        this.translateServiceAddress = translateServiceAddress;
        executorService.scheduleAtFixedRate(() -> {
            System.out.println(new Date() + ": " + results);
        }, 10, 10, TimeUnit.SECONDS);
    }


    @GetMapping("/")
    public String index() {
        return "Greetings from " + spaceName + " space main app!" +
                "<br>Try the following endpoints:" +
                "<br>  /testCall" +
                "<br>  /go" +
                "<br>  /helloGrps?name=Ivan";
    }

    @GetMapping("/testCall")
    public String testCall() {
//        System.out.println("on testCall");
//        return restTemplate.getForObject(translateServiceAddress + "/ping", String.class);
        return restClient.get()
                .uri(translateServiceAddress + "/ping")
                .retrieve()
                .body(String.class);
    }

    @GetMapping("/log")
    public String log(@RequestParam(defaultValue = "false") boolean enabled) {
        logRequestResults.set(enabled);
        return "ok";
    }

    @GetMapping("/go")
    public String go() {
        executorService.scheduleAtFixedRate(() -> {
            try {
                String result = testCall();
                results.merge(result, 1, Integer::sum);

                if (logRequestResults.get() && requestCounter.incrementAndGet() % 13 == 0) {
                    System.out.println(new Date() + ": " + result);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, 10, 10, TimeUnit.MILLISECONDS);

        return "ok";
    }

    @GetMapping("/helloGrps")
    public String helloGrps(@RequestParam String name) {
        return grpsClient.sayHello(name);
    }
}
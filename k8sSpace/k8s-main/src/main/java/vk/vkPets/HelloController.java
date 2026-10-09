package vk.vkPets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import vk.vkPets.discovery.InClusterServiceDiscovery;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class HelloController {
    private final Map<String, Integer> results = new ConcurrentHashMap<>();
    private final AtomicInteger requestCounter = new AtomicInteger();
    private final AtomicBoolean logRequestResults = new AtomicBoolean();

    @Value("${spring.application.name}")
    private String appName;
    private final String translateServiceAddress;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final RestClient restClient;
    private final SpringGrpsClient grpsClient;

    public HelloController(RestClient restClient, SpringGrpsClient grpsClient,
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
        return "Greetings from " + appName + " "
                + "<br>Try the following endpoints:"
                + "<br>  /testHttpCall"
                + "<br>  /testGrpsCall"
                + "<br>  /goHttp"
                + "<br>  /goGrps"
                + "<br>  /testDiscoveryWatcher";
    }

    @GetMapping("/testHttpCall")
    public String testHttpCall() {
//        System.out.println("on testCall");
//        return restTemplate.getForObject(translateServiceAddress + "/ping", String.class);
        return restClient.get()
                .uri(translateServiceAddress + "/ping")
                .retrieve()
                .body(String.class);
    }

    @GetMapping("/testGrpsCall")
    public String testGrpsCall() {
        return grpsClient.sayHello("test");
    }

    @GetMapping("/log")
    public String log(@RequestParam(defaultValue = "false") boolean enabled) {
        logRequestResults.set(enabled);
        return "ok";
    }

    @GetMapping("/goHttp")
    public String goHttp() {
        return goImpl(this::testHttpCall);
    }

    @GetMapping("/goGrps")
    public String goGrps() {
        return goImpl(this::testGrpsCall);
    }

    private String goImpl(Callable<String> action) {
        executorService.scheduleAtFixedRate(() -> {
            try {
                String result = action.call();
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

    @GetMapping("/testDiscoveryWatcher")
    public String testDiscoveryWatcher(@RequestParam String k8sUrl) {
        new Thread(() -> {
            try {
                String[] args = (k8sUrl == null || k8sUrl.isBlank()) ? new String[] {} : new String[] {k8sUrl};
                InClusterServiceDiscovery.main(args);
            } catch (Exception e) {
                System.out.println(e.toString());
                e.printStackTrace();
            }
        }).start();
        return "ok";
    }

    public void goKafka() {
        throw new UnsupportedOperationException();
    }
}
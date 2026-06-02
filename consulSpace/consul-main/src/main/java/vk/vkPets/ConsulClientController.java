package vk.vkPets;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * uses consul client directly
 */
@RestController
public class ConsulClientController {
//    private final ConsulClient client;
//
//    public ConsulClientController(
//            @Value("${spring.cloud.consul.host}") String consulHost,
//            @Value("${spring.cloud.consul.port}") int consulPort)
//    {
//        client  = new ConsulClient(consulHost, consulPort);
//    }


    @GetMapping("/nodes")
    public List<String> getNodes(@RequestParam String service) {
        throw new UnsupportedOperationException("not implemented");
    }

    @GetMapping("/services")
    public List<String> getServices() {
//        return new ArrayList<>(client.getAgentServices().getValue().keySet());

        throw new UnsupportedOperationException("not implemented");
    }

}

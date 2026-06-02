package vk.vkPets;

import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.NotRegisteredException;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.cache.ServiceHealthKey;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import com.orbitz.consul.model.health.ServiceHealth;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This client API is ARCHIVED !!!
 * See https://github.com/rickfast/consul-client
 */
public class ConsulOrbitzSandbox {
    static ServiceHealthCache svHealth;

    public static void main(String[] args) throws InterruptedException, NotRegisteredException {
        Consul consul = Consul.builder()
                .withUrl("http://localhost:8500")  // Connects to localhost:8500 by default
                .build();

        String serviceId = "myServiceId";
        subscribeForChanges(consul, serviceId);
        register(consul, serviceId, "127.0.0.1");
        register(consul, serviceId, "127.0.0.2");

        discover(consul, serviceId);

        Thread.sleep(10_000);

        // Deregister when done
        consul.agentClient().deregister(serviceId);

        Thread.sleep(3_000);
        svHealth.stop();
    }

    private static void subscribeForChanges(Consul consul, String serviceId) {
        HealthClient healthClient = consul.healthClient();

        svHealth = ServiceHealthCache.newCache(healthClient, serviceId);
        svHealth.addListener((Map<ServiceHealthKey, ServiceHealth> newValues) -> {
            for (ServiceHealth instance : newValues.values()) {
                System.out.println("UPDATE: " + instance.getService().getAddress());
            }
        });
        svHealth.start();
    }

    private static void discover(Consul consul, String serviceId) {
        HealthClient healthClient = consul.healthClient();

        List<ServiceHealth> allNodes = consul.healthClient().getAllServiceInstances(serviceId).getResponse();
        allNodes.forEach(instance ->
            System.out.println("ALL Instance: " + instance.getService().getAddress())
        );

        // Discover only "passing" nodes
        List<ServiceHealth> healthyNodes = healthClient.getHealthyServiceInstances(serviceId).getResponse();
        healthyNodes.forEach(instance ->
                System.out.println("H Instance: " + instance.getService().getAddress())
        );
    }

    private static void register(Consul consul, String serviceId, String address) throws NotRegisteredException {
        Registration service = ImmutableRegistration.builder()
                .id(serviceId)
                .name("myService")
                .address(address)
                .port(8080)
                .check(Registration.RegCheck.ttl(3L)) // registers with a TTL of 3 seconds
                .tags(Collections.singletonList("tag1"))
                .meta(Collections.singletonMap("version", "1.0"))
                .build();

        consul.agentClient().register(service);

        // Check in with Consul (serviceId required only).
        // Client will prepend "service:" for service level checks.
        // Note that you need to continually check in before the TTL expires, otherwise your service's state will be marked as "critical".
        // todo: do it periodically
        consul.agentClient().pass(serviceId);
    }
}
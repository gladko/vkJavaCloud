package vk.vkPets;

import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;

public class ConsulOrbitzSandbox {
    public static void main(String[] args) throws InterruptedException {
        Consul consul = Consul.builder()
                .withUrl("http://localhost:8500")  // Connects to localhost:8500 by default
                .build();

        Registration service = ImmutableRegistration.builder()
                .id("myServiceId")
                .name("myService")
                .address("127.0.0.1")
                .port(8080)
                .build();

        consul.agentClient().register(service);

        // Query service
        consul.healthClient().getAllServiceInstances("myService").getResponse().forEach(instance -> {
            System.out.println("Instance: " + instance.getService().getAddress());
        });

        Thread.sleep(10_000);

        // Deregister when done
        consul.agentClient().deregister("myServiceId");
    }
}
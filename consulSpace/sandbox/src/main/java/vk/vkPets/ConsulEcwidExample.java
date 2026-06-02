package vk.vkPets;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;


public class ConsulEcwidExample {
    public static void main(String[] args) throws InterruptedException {
        ConsulClient client = new ConsulClient("localhost");

        NewService newService = new NewService();
        newService.setId("serviceId");
        newService.setName("myService");
        newService.setAddress("127.0.0.1");
        newService.setPort(8080);

        client.agentServiceRegister(newService);

        // ... use client.healthClient() or other methods to query
//        client.getHealthServices()

        Thread.sleep(10_000);

        client.agentServiceDeregister("serviceId");
    }
}

package vk.vkPets;

import com.example.grpc.GreetingServiceGrpc;
import com.example.grpc.HelloRequest;
import org.springframework.stereotype.Service;

@Service
public class SpringGrpsClient {
    @net.devh.boot.grpc.client.inject.GrpcClient("greeting")
    private GreetingServiceGrpc.GreetingServiceBlockingStub stub;

    public String sayHello(String name) {
        return stub.sayHello(
                        HelloRequest.newBuilder()
                                .setName(name)
                                .build())
                .getMessage();
    }
}

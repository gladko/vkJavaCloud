package vk.vkPets;

import com.example.grpc.GreetingServiceGrpc;
import com.example.grpc.HelloRequest;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;


@Service
public class GreetingsService {

    @GrpcClient("greeting")
    private GreetingServiceGrpc.GreetingServiceBlockingStub stub;

    public String sayHello(String name) {
        return stub.sayHello(
                        HelloRequest.newBuilder()
                                .setName(name)
                                .build())
                .getMessage();
    }


}

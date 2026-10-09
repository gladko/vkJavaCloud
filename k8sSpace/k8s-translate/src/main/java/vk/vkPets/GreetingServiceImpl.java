package vk.vkPets;


import com.example.grpc.GreetingServiceGrpc;
import com.example.grpc.HelloRequest;
import com.example.grpc.HelloResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class GreetingServiceImpl extends GreetingServiceGrpc.GreetingServiceImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        System.out.println("sayHello");
        HelloResponse response = HelloResponse.newBuilder()
                .setMessage("Hello " + request.getName() + "from " + K8sTranslateApp.HOST_NAME)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
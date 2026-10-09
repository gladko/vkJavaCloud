package vk.vkPets;

import com.example.grpc.GreetingServiceGrpc;
import com.example.grpc.HelloRequest;
import com.example.grpc.HelloResponse;
import io.grpc.*;
import io.grpc.xds.XdsChannelCredentials;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PlainGrpsClient {

    public static void main(String[] args) throws InterruptedException {
        testCall("localhost:9090");
    }

    public static void testCall(String target) throws InterruptedException {
        // 1. Create a communication channel to the server
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                // usePlaintext skips TLS/SSL. Use only for testing/local dev!
                .usePlaintext()
                .build();

        try {
            // 2. Create a synchronous blocking stub
            GreetingServiceGrpc.GreetingServiceBlockingStub stub =
                    GreetingServiceGrpc.newBlockingStub(channel);

            // 3. Build the request payload
            HelloRequest request = HelloRequest.newBuilder()
                    .setName("Java Developer")
                    .build();

            // 4. Make the remote procedure call (RPC)
            System.out.println("Sending request to server...");
            HelloResponse response = stub.sayHello(request);

            // 5. Handle the response
            System.out.println("Received from server: " + response.getMessage());

        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
        } finally {
            // 6. Properly shut down the channel when done
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }


    public static void test_xDS_call(String name) {
        // 1. SAFETY CHECK: gRPC xDS demands a local configuration bootstrap file path
        if (System.getenv("GRPC_XDS_BOOTSTRAP") == null) {
            System.err.println("❌ ERROR: GRPC_XDS_BOOTSTRAP environment variable is not defined!");
            System.err.println("👉 Proxyless xDS requires a bootstrap JSON file to discover the control plane.");
            return;
        }

        // 2. Target your Service Mesh internal cluster resource name.
        // Format: xds:///short-svc-name.namespace.svc.cluster.local:port
        String targetXdsUri = "xds:///k8s-translate.default.svc.cluster.local:8080";

        System.out.println("📡 Initializing Proxyless xDS managed channel...");
        System.out.println("🎯 Targeting: " + targetXdsUri);

        // 3. Instantiate the xDS client engine.
        // XdsChannelCredentials fallback to standard plaintext (Insecure) if the mesh lacks TLS configurations.
        ManagedChannel channel = Grpc.newChannelBuilder(
                targetXdsUri,
                XdsChannelCredentials.create(InsecureChannelCredentials.create())
        ).build();

        // 4. Create your traditional communication stub wrapper
        GreetingServiceGrpc.GreetingServiceBlockingStub blockingStub =
                GreetingServiceGrpc.newBlockingStub(channel);

        // 5. Infinite Service Discovery Execution Loop
        System.out.println("🟢 Connection active! Processing gRPC calls with active load-balancing...");
        while (true) {
            try {
                long startTime = System.currentTimeMillis();

                // Formulate your message contract payload
                HelloRequest request = HelloRequest.newBuilder()
                        .setName(name)
                        .build();

                // Fire request over the xDS routing layer
                HelloResponse response = blockingStub.sayHello(request);

                long duration = System.currentTimeMillis() - startTime;
                System.out.printf("[SUCCESS] Translated: '%s' (Latency: %dms)%n",
                        response.getMessage(), duration);

            } catch (StatusRuntimeException e) {
                // If a container fails or scales down during transit, the loop logs the drop and continues.
                // The internal xDS channel will automatically evict dead routes behind the scenes.
                System.err.println("💥 RPC Execution Failed. Status: " + e.getStatus() + " | " + e.getMessage());
            } catch (Exception e) {
                System.err.println("❌ General Processing Failure: " + e.getMessage());
            }

            // Sleep interval before firing the next request downstream
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("🛑 Loop interrupted. Shutting down client channel pipeline.");
                break;
            }
        }

        // 6. Graceful cleanup sequence upon termination
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}


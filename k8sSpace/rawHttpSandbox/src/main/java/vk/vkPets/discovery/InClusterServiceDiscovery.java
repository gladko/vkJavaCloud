package vk.vkPets.discovery;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class InClusterServiceDiscovery {

    // Native standard Kubernetes in-cluster mount paths
    private static final String TOKEN_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";
    private static final String CA_PATH    = "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt";
    private static final String NS_PATH    = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";

    public static void main(String[] args) {
        // Internal K8s control plane DNS name accessible inside any Pod
        String k8sServerUrl = args.length == 0 ? "https://kubernetes.default.svc" : args[0];
        ObjectMapper mapper = new ObjectMapper();

        try {
            // 1. Read the current namespace dynamically from the disk file
            String namespace = Files.readString(Path.of(NS_PATH)).trim();
            String watchUrl = k8sServerUrl + "/api/v1/namespaces/" + namespace + "/endpoints?watch=true";

            // 2. Build a SECURE HTTP Client using the cluster's native ca.crt file
            HttpClient client = createSecureInClusterHttpClient();

            System.out.println("📡 In-Cluster Engine Active. Monitoring namespace: " + namespace);

            while (true) {
                try {
                    // 3. FIXED: Read token from disk *inside* the loop.
                    // If K8s rotates the token file on disk, your next reconnect instantly picks it up!
                    String token = Files.readString(Path.of(TOKEN_PATH)).trim();

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(watchUrl))
                            .header("Authorization", "Bearer " + token)
                            .header("Accept", "application/json")
                            .GET()
                            .build();

                    HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                    if (response.statusCode() == 200) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                K8sEndpointEvent event = mapper.readValue(line, K8sEndpointEvent.class);
                                // processDiscoveryEvent(event);
                                System.out.println("Received change: " + event.type());
                            }
                        }
                    } else {
                        throw new RuntimeException("K8s API Server rejected token. HTTP Code: " + response.statusCode());
                    }

                } catch (Exception e) {
                    System.err.println("💥 Stream disconnected. Reconnecting in 5s... Error: " + e.getMessage());
                    e.printStackTrace();
                }

                Thread.sleep(5000); // 5-second recovery backoff delay
            }
        } catch (Exception e) {
            System.err.println("❌ Fatal initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Builds a secure HttpClient that trusts the internal Kubernetes Root CA explicitly.
     * Bypasses the need for your older "trust-all-certs" workaround.
     */
    private static HttpClient createSecureInClusterHttpClient() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert;
        try (InputStream certStream = Files.newInputStream(Path.of(CA_PATH))) {
            caCert = (X509Certificate) cf.generateCertificate(certStream);
        }

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("k8s-ca", caCert);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        return HttpClient.newBuilder().sslContext(sslContext).build();
    }
}

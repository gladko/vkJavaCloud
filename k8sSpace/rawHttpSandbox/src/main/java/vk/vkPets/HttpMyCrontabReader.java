package vk.vkPets;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class HttpMyCrontabReader {
    static String K8S_SERVER = "https://127.0.0.1:6443";
    static String API_CRONTABS = "/apis/stable.example.com/v1/namespaces/default/crontabs";

    public static void main(String[] args) throws Exception {
        String targetUrl = K8S_SERVER + API_CRONTABS + "/nightly-backup-cron";
        String token = AuthTokenProvider.getToken();

        HttpClient client = createHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            parseByJackson(response);
        } else {
            System.err.println("❌ K8s API Returned Error Code: " + response.statusCode());
        }
    }

    static HttpClient createHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
        // Omit SSL validation for local K3s self-signed certificates
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();
    }

    private static void parseByJackson(HttpResponse<String> response) throws JsonProcessingException {
        System.out.println("response: " + response.body());

        ObjectMapper mapper = new ObjectMapper();
        CronTab cronTab = mapper.readValue(response.body(), CronTab.class);

        System.out.println("\n==================================================");
        System.out.println("📦 CRONTAB PARSED SUCCESSFULLY VIA JACKSON");
        System.out.println("==================================================");
        System.out.printf("🔹 Config Name: %s%n", cronTab.getMetadata().getName());
        System.out.printf("⏱️  Cron Stmt:  %s%n", cronTab.getSpec().getCronStmt());
        System.out.printf("🖼️  Icon Path:  %s%n", cronTab.getSpec().getIconPath());
        System.out.println("==================================================");
    }
}

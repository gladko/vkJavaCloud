package vk.vkPets;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * run this class and then exec `kubectl delete crontab weekly-report-cron`.
 */
public class HttpMyCrontabWatcher {
    public static void main(String[] args) throws Exception {
        // Appending ?watch=true turns the endpoint into a continuous server-sent streaming pipeline
        String targetUrl = HttpMyCrontabReader.K8S_SERVER + HttpMyCrontabReader.API_CRONTABS + "?watch=true";
        String token = AuthTokenProvider.getToken();

        HttpClient client = HttpMyCrontabReader.createHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        System.out.println("📡 Connecting to live Kubernetes Stream... (Press Ctrl+C to stop)");

        // Request the body as an InputStream so we can parse lines lazily as they stream in
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                String line;
                // This loop hangs waiting patiently for the next chunk from Kubernetes
                while ((line = reader.readLine()) != null) {
                    try {
//                        System.out.println("got: " + line);
                        processUpdate(mapper, line);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.err.println("❌ Stream Connection Failed. Code: " + response.statusCode());
        }
    }

    private static void processUpdate(ObjectMapper mapper, String line) throws JsonProcessingException {
        // Kubernetes streams watch events inside an envelope: {"type": "ADDED"|"MODIFIED"|"DELETED", "object": {...}}
        JsonNode eventEnvelope = mapper.readTree(line);
        String eventType = eventEnvelope.get("type").asText();

        // Parse the inner "object" node straight into our strong Java data type
        JsonNode objectNode = eventEnvelope.get("object");
        CronTab cronTab = mapper.treeToValue(objectNode, CronTab.class);

        System.out.printf("[%s] Name: %-20s | Stmt: %-10s | Icon: %s%n",
                eventType,
                cronTab.getMetadata().getName(),
                cronTab.getSpec().getCronStmt(),
                cronTab.getSpec().getIconPath()
        );
    }
}

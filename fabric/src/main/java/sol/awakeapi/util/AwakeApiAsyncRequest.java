package sol.awakeapi.util;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.api.api_data.AIParams;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class AwakeApiAsyncRequest {

    private static final String SIMPLE_NAME = AwakeApiAsyncRequest.class.getSimpleName();

    private static HttpClient OAILikeHttpClient;
    private static CloseableHttpClient OobaHttpClient;

    public static void registerHttpClients() {
        AwakeApi.LOGGER.info("@{}: Registering HttpClients.", SIMPLE_NAME);
        OAILikeHttpClient = HttpClient.newHttpClient();
        OobaHttpClient = HttpClients.createDefault();
    }

    public static CompletableFuture<String> sendAsyncRequestToAI(String jsonRequestBody, AIParams params) {
        if (params.isOoba()) {
            return sendRequestToOoba(jsonRequestBody, params);
        } else {
            return sendRequestToOAILike(jsonRequestBody, params);
        }
    }

    private static CompletableFuture<String> sendRequestToOoba(String jsonRequestBody, AIParams params) {
        AwakeApi.LOGGER.info("@{}: Sending asynchronous request to locally hosted model.", SIMPLE_NAME);

        HttpPost request = new HttpPost(params.getEndpoint());
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(jsonRequestBody, "UTF-8"));

        if (params.getKey() != null && !params.getKey().isEmpty()) {
            request.setHeader("Authorization", "Bearer " + params.getKey());
        }

        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpResponse response = OobaHttpClient.execute(request)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder responseContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                return responseContent.toString();
            } catch (Exception e) {
                System.err.println("Error during API call: " + e.getMessage());
                return "Error: " + e.getMessage();
            }
        });
    }

    private static CompletableFuture<String> sendRequestToOAILike(String jsonRequestBody, AIParams params) {
        AwakeApi.LOGGER.info("@{}: Sending asynchronous request to OAI like.", SIMPLE_NAME);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(params.getEndpoint()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody));

        if (params.getKey() != null && !params.getKey().isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + params.getKey());
        }

        HttpRequest request = requestBuilder.build();

        return OAILikeHttpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(e -> {
                    System.err.println("Error during API call: " + e.getMessage());
                    return "Error: " + e.getMessage();
                });
    }
}
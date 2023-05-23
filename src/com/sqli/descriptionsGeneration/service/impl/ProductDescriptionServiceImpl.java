package com.sqli.descriptionsGeneration.service.impl;

import com.sqli.descriptionsGeneration.service.ProductDescriptionService;
import com.sqli.descriptionsHttpClient.service.HttpClientService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.json.Json;
import javax.json.JsonObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class ProductDescriptionServiceImpl implements ProductDescriptionService {
    @Value("${OpenAI.API_KEY}")
    private String API_KEY ;
    @Value("${OpenAI.API_URL}")
    private String API_URL;

    public String generateProductDescription(String productName, String features) throws Exception {
        String prompt = createPrompt(productName, features);
        JsonObject body = createRequestBody(prompt);
        URL url = new URL(this.API_URL);
        HttpURLConnection httpURLConnection = createConnection(url);
        sendRequest(httpURLConnection, body);
        String response = getResponse(httpURLConnection);
        String description = extractDescriptionText(response, prompt);
        return description;
    }

    public String createPrompt(String productName, String features) {
        return "Product: " + productName + "\n\nFeatures: " + features + "\n\nDescription:";
    }

    public JsonObject createRequestBody(String prompt) {
        return Json.createObjectBuilder()
                .add("prompt", prompt)
                .add("max_tokens", 100)
                .add("temperature", 0)
                .add("n", 1)
                .build();
    }

    public String extractDescriptionText(String response, String prompt) {
        try {
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray choicesArray = jsonResponse.getJSONArray("choices");
            String text = choicesArray.getJSONObject(0).getString("text");
            text = text.trim().replace(prompt, "");
            return text;
        } catch (Exception e ){
            return e.getMessage();
        }
    }
    @Override
    public HttpURLConnection createConnection(URL url) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + this.API_KEY);
        httpURLConnection.setDoOutput(true);
        return httpURLConnection;
    }
    @Override
    public  void sendRequest(HttpURLConnection httpURLConnection, JsonObject body) throws Exception {
        httpURLConnection.getOutputStream().write(body.toString().getBytes());
        httpURLConnection.getOutputStream().flush();
        httpURLConnection.getOutputStream().close();
    }
    @Override
    public  String getResponse(HttpURLConnection httpURLConnection) throws Exception {
        StringBuilder response = new StringBuilder();
        try (InputStream is = httpURLConnection.getInputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                response.append(new String(buffer, 0, bytesRead));
            }
        }
        return response.toString();
    }
}

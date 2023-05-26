package com.sqli.descriptionsGeneration.service.impl;
import com.sqli.descriptionsGeneration.service.ProductDescriptionService;
import com.sqli.descriptionsHttpClient.service.HttpClientService;
import com.sqli.exceptions.GenerationException;
import com.sqli.exceptions.HttpClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.json.Json;
import javax.json.JsonObject;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class ProductDescriptionServiceImpl implements ProductDescriptionService {
    @Value("${OpenAI.API_URL}")
    private String API_URL;
    private HttpClientService httpClientService;
    public ProductDescriptionServiceImpl(HttpClientService httpClientService){
        this.httpClientService = httpClientService;
    }
    @Override
    public String generateProductDescription(String productName, String features) throws GenerationException, HttpClientException, MalformedURLException {
        String prompt = createPrompt(productName, features);
        JsonObject body = createRequestBody(prompt);

        URL url = new URL(this.API_URL);
        HttpURLConnection httpURLConnection = this.httpClientService.createConnection(url);
        this.httpClientService.sendRequest(httpURLConnection, body);
        String response = this.httpClientService.getResponse(httpURLConnection);

        return this.httpClientService.parseDescripitonTextFromResponse(response, prompt);
    }
    @Override
    public String createPrompt(String productName, String features) {
        return "Product: " + productName + "\n\nFeatures: " + features + "\n\nDescription:";
    }
    @Override
    public JsonObject createRequestBody(String prompt) {
        return Json.createObjectBuilder()
                .add("prompt", prompt)
                .add("max_tokens", 100)
                .add("temperature", 0)
                .add("n", 1)
                .build();
    }

}

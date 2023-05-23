package com.sqli.descriptionsGeneration.service;

import javax.json.JsonObject;
import java.net.HttpURLConnection;
import java.net.URL;

public interface ProductDescriptionService {
   String generateProductDescription(String productName, String features) throws Exception;
    String createPrompt(String productName, String features);
    JsonObject createRequestBody(String prompt);
    String extractDescriptionText(String response, String prompt);
}

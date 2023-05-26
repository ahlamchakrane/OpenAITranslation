package com.sqli.descriptionsGeneration.service;
import com.sqli.exceptions.GenerationException;
import com.sqli.exceptions.HttpClientException;

import javax.json.JsonObject;
import java.net.MalformedURLException;

public interface ProductDescriptionService {
   String generateProductDescription(String productName, String features) throws GenerationException, HttpClientException, MalformedURLException;
    String createPrompt(String productName, String features);
    JsonObject createRequestBody(String prompt);
}

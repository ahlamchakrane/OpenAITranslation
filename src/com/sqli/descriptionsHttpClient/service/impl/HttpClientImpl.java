package com.sqli.descriptionsHttpClient.service.impl;

import com.sqli.descriptionsHttpClient.service.HttpClientService;
import com.sqli.exceptions.GenerationException;
import com.sqli.exceptions.HttpClientException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import javax.json.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpClientImpl implements HttpClientService {
    @Value("${OpenAI.API_KEY}")
    private String API_KEY ;
    @Override
    public HttpURLConnection createConnection(URL url) throws HttpClientException {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + this.API_KEY);
            httpURLConnection.setDoOutput(true);
            return httpURLConnection;
        } catch (IOException e) {
            throw new HttpClientException("Failed to create HTTP connection.", e);
        }

    }
    @Override
    public  void sendRequest(HttpURLConnection httpURLConnection, JsonObject body) throws HttpClientException {
        try {
            httpURLConnection.getOutputStream().write(body.toString().getBytes());
            httpURLConnection.getOutputStream().flush();
            httpURLConnection.getOutputStream().close();
        } catch (IOException e) {
            throw new HttpClientException("Failed to send HTTP request.", e);
        }
    }
    @Override
    public  String getResponse(HttpURLConnection httpURLConnection) throws HttpClientException {
        StringBuilder response = new StringBuilder();
        try (InputStream is = httpURLConnection.getInputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                response.append(new String(buffer, 0, bytesRead));
            }
        } catch (IOException e) {
            throw new HttpClientException("Failed to get HTTP response.", e);
        }
        return response.toString();
    }
    @Override
    public Map<String, String> parseTranslatedTextsFromResponse(String response, String prompt) throws GenerationException {
        Map<String, String> translations = new HashMap<>();

        try {
            String text = new JSONObject(response)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
                    .replace(prompt, "");

            Arrays.stream(text.split("\n"))
                    .map(segment -> segment.split(": ", 2))
                    .filter(parts -> parts.length == 2)
                    .forEach(parts -> translations.put(parts[0], parts[1]));

        } catch (Exception e) {
            throw new GenerationException("Failed to parse translated text from response.", e);
        }

        return translations;
    }

    @Override
    public String parseDescripitonTextFromResponse(String response, String prompt) throws GenerationException {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray choicesArray = jsonResponse.getJSONArray("choices");
            String text = choicesArray.getJSONObject(0).getString("text");
            return text.trim().replace(prompt, "");
        } catch (Exception e) {
            throw new GenerationException("Failed to parse translated text from response.", e);
        }
    }
}

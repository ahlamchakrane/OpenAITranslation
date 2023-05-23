package com.sqli.descriptionsHttpClient.service.impl;

import com.sqli.descriptionsHttpClient.service.HttpClientService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import javax.json.JsonObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClientImpl implements HttpClientService {
    @Value("${OpenAI.API_KEY}")
    private String API_KEY ;
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
    public String parseTranslatedTextFromResponse(String response, String prompt) {
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
}

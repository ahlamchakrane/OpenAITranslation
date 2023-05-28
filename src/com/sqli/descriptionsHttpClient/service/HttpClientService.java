package com.sqli.descriptionsHttpClient.service;

import com.sqli.exceptions.GenerationException;
import com.sqli.exceptions.HttpClientException;
import org.json.JSONException;

import javax.json.JsonObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public interface HttpClientService {
    HttpURLConnection createConnection(URL apiUrl) throws HttpClientException;
    void sendRequest(HttpURLConnection httpURLConnection, JsonObject body) throws HttpClientException;
    String getResponse(HttpURLConnection httpURLConnection) throws HttpClientException;
    String extractResponseText(HttpURLConnection httpURLConnection, String prompt) throws JSONException, HttpClientException;
    JsonObject createRequestBody(String prompt, int maxTokens);


}

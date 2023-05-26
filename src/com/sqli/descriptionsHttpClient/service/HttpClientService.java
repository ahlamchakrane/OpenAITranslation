package com.sqli.descriptionsHttpClient.service;

import com.sqli.exceptions.GenerationException;
import com.sqli.exceptions.HttpClientException;

import javax.json.JsonObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public interface HttpClientService {
    HttpURLConnection createConnection(URL apiUrl) throws HttpClientException;
    void sendRequest(HttpURLConnection httpURLConnection, JsonObject body) throws HttpClientException;
    String getResponse(HttpURLConnection httpURLConnection) throws HttpClientException;
    Map<String, String> parseTranslatedTextsFromResponse(String response, String prompt) throws GenerationException;
    String parseDescripitonTextFromResponse(String response, String prompt) throws GenerationException;

}

package com.sqli.descriptionsHttpClient.service;

import javax.json.JsonObject;
import java.net.HttpURLConnection;
import java.net.URL;

public interface HttpClientService {
    HttpURLConnection createConnection(URL apiUrl) throws Exception;
    void sendRequest(HttpURLConnection httpURLConnection, JsonObject body) throws Exception;
    String getResponse(HttpURLConnection httpURLConnection) throws Exception;
    String parseTranslatedTextFromResponse(String response, String prompt);
}

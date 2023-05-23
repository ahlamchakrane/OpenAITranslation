package com.sqli.descriptionsHttpClient.service;

import javax.json.JsonObject;
import java.net.HttpURLConnection;
import java.net.URL;

public interface HttpClientService {
    HttpURLConnection createConnection(URL apiUrl) throws Exception;
    void sendRequest(HttpURLConnection conn, JsonObject body) throws Exception;
    String getResponse(HttpURLConnection conn) throws Exception;
}

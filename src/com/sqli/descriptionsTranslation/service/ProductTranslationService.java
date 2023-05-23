package com.sqli.descriptionsTranslation.service;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;

import javax.json.JsonObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public interface ProductTranslationService {
    List<ProductModel> getAllProducts();
    List<LanguageModel> getLanguagesFromProductFeatures(ProductModel product);
    String parseTranslatedTextFromResponse(String response);
    void saveTranslatedDescription(ProductModel product, LanguageModel language, String translatedDescription);
    String generateTranslation(String description, String targetLanguage);
    String createPrompt(String targetLanguage);
    HttpURLConnection createConnection(URL apiUrl) throws Exception;
    void sendRequest(HttpURLConnection conn, JsonObject body) throws Exception;
    String getResponse(HttpURLConnection conn) throws Exception;
}

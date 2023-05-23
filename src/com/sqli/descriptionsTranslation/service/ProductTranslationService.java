package com.sqli.descriptionsTranslation.service;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;

import javax.json.JsonObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public interface ProductTranslationService {
    List<ProductModel> getAllProducts();
    List<String> getLanguages();
    String parseTranslatedTextFromResponse(String response);
    void saveTranslatedDescription(ProductModel product, String language, String translatedDescription);
    String generateTranslation(String description, String targetLanguage);
    String createPrompt(String targetLanguage);
}

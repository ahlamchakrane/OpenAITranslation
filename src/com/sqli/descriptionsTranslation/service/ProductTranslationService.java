package com.sqli.descriptionsTranslation.service;
import com.sqli.exceptions.GenerationException;
import com.sqli.exceptions.HttpClientException;
import de.hybris.platform.core.model.product.ProductModel;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public interface ProductTranslationService {
    List<ProductModel> getAllProducts();
    List<String> getLanguages();
    void saveTranslatedDescription(ProductModel product, String language, String translatedDescription);
    Map<String, String> generateTranslations(String description, List<String> targetLanguages) throws HttpClientException, GenerationException, MalformedURLException;

    String createPrompt(List<String> targetLanguages);
}

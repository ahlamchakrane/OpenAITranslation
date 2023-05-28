package com.sqli.descriptionsTranslation.service;
import com.sqli.exceptions.GenerationException;
import com.sqli.exceptions.HttpClientException;
import de.hybris.platform.core.model.product.ProductModel;
import org.json.JSONException;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public interface ProductTranslationService {
    List<ProductModel> getAllProducts();
    List<ProductModel> getProductById(String productId);
    List<String> getAllLanguages();
    List<ProductModel> getAllProductsNeedsTranslation();
    List<String> getAllAvailableLanguages();
    void translateProductDescriptions(ProductModel product, List<String> languages) throws MalformedURLException, HttpClientException, GenerationException, JSONException;
    Map<String, String> generateTranslations(String description, List<String> targetLanguages) throws HttpClientException, GenerationException, MalformedURLException, JSONException;
    Map<String, String> processGenerationTranslation(String description, List<String> targetLanguages) throws HttpClientException, GenerationException, MalformedURLException, JSONException;
}

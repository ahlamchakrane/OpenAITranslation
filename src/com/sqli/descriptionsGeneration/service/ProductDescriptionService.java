package com.sqli.descriptionsGeneration.service;
import com.sqli.exceptions.GenerationException;
import com.sqli.exceptions.HttpClientException;
import de.hybris.platform.classification.features.Feature;
import de.hybris.platform.classification.features.FeatureList;
import de.hybris.platform.core.model.product.ProductModel;

import javax.json.JsonObject;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface ProductDescriptionService {
    List<ProductModel> getProductsWithoutDescription();
    String generateProductDescription(String productName, Map<String, List<String>> featureDescriptions) throws MalformedURLException, HttpClientException, GenerationException;
    String generateDescriptionFromAPI(String productName, String features) throws GenerationException, HttpClientException, MalformedURLException;
    List<String> getLanguages();
    List<ProductModel> getProducts(String condition, Map<String, Object> params);
    List<ProductModel> getProductById(String productId);
    boolean hasNonEmptyFeatures(ProductModel product);
    List<ProductModel> filterProductsWithoutDescription(List<ProductModel> products);
    void processGenerationDescription(ProductModel product);

}

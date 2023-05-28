package com.sqli.descriptionsGeneration.service.impl;
import com.sqli.descriptionsGeneration.service.ProductDescriptionService;
import com.sqli.descriptionsHttpClient.service.HttpClientService;
import com.sqli.exceptions.GenerationException;
import com.sqli.exceptions.HttpClientException;
import com.sqli.service.impl.DefaultOpenAIAutoDescriptionGeneratorService;
import de.hybris.platform.classification.ClassificationService;
import de.hybris.platform.classification.features.Feature;
import de.hybris.platform.classification.features.FeatureList;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.json.Json;
import javax.json.JsonObject;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ProductDescriptionServiceImpl implements ProductDescriptionService {
    @Value("${OpenAI.API_URL}")
    private String API_URL;
    private final HttpClientService httpClientService;
    private final FlexibleSearchService flexibleSearchService;
    private final ClassificationService classificationService;
    private final ModelService modelService;
    private static final Logger LOG = LoggerFactory.getLogger(DefaultOpenAIAutoDescriptionGeneratorService.class);

    public ProductDescriptionServiceImpl(HttpClientService httpClientService,
                                         FlexibleSearchService flexibleSearchService,
                                         ClassificationService classificationService,
                                         ModelService modelService){
        this.httpClientService = httpClientService;
        this.classificationService = classificationService;
        this.modelService = modelService;
        this.flexibleSearchService = flexibleSearchService;

    }
    @Override
    public List<ProductModel> getProducts(String condition, Map<String, Object> params) {
        String query = "SELECT {p:pk} FROM {Product AS p JOIN CatalogVersion AS cv ON {p.catalogVersion}={cv.pk}} " + condition;
        FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query);

        params.forEach(searchQuery::addQueryParameter);

        SearchResult<ProductModel> searchResult = flexibleSearchService.search(searchQuery);
        return searchResult.getResult();
    }
    @Override
    public List<ProductModel> getProductsWithoutDescription() {
        List<ProductModel> products = getProducts("WHERE {cv.version}='Staged'", Collections.emptyMap());
        return filterProductsWithoutDescription(products);
    }
    @Override
    public List<ProductModel> getProductById(String productId) {
        List<ProductModel> products = getProducts("WHERE {cv.version}='Staged' AND {p.code}=?productId",
                Collections.singletonMap("productId", productId));
        return filterProductsWithoutDescription(products);
    }
    @Override
    public List<ProductModel> filterProductsWithoutDescription(List<ProductModel> products) {
        List<ProductModel> productsWithoutDescription = new ArrayList<>();
        List<String> languages = getLanguages();
        for (ProductModel product : products) {
            boolean hasDescription = languages.stream()
                    .anyMatch(language -> {
                        Locale locale = getLocaleFromLanguage(language);
                        String description = product.getDescription(locale);
                        return description != null && !description.isEmpty();
                    });

            if (!hasDescription) {
                productsWithoutDescription.add(product);
            }
        }
        return productsWithoutDescription;
    }
    @Override
    public List<String> getLanguages() {
        String query = "SELECT DISTINCT {l:pk} FROM {Language AS l}";
        List<LanguageModel> languages = performSearch(query, Collections.emptyMap());
        return languages.stream()
                .map(LanguageModel::getIsocode)
                .collect(Collectors.toList());
    }
    private Locale getLocaleFromLanguage(String language) {
        String[] parts = language.split("_");
        String languagePart = parts[0].toLowerCase();
        String countryPart = (parts.length > 1) ? parts[1].toUpperCase() : "";
        return new Locale(languagePart, countryPart);
    }
    private <T> List<T> performSearch(String query, Map<String, Object> params) {
        FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query);
        params.forEach(searchQuery::addQueryParameter);
        SearchResult<T> searchResult = flexibleSearchService.search(searchQuery);
        return searchResult.getResult();
    }
    @Override
    public boolean hasNonEmptyFeatures(ProductModel product) {
        FeatureList featuresList = classificationService.getFeatures(product);
        return !featuresList.getFeatures().isEmpty() &&
                featuresList.getFeatures().stream().anyMatch(feature -> !feature.getValues().isEmpty());
    }
    @Override
    public void processGenerationDescription(ProductModel product) {
        try {
            String productName = product.getName();
            FeatureList featuresList = classificationService.getFeatures(product);
            List<Feature> features = filterFeaturesWithValues(featuresList);

            Map<String, List<String>> featureDescriptions = generateFeatureDescriptions(features);
            String description = generateProductDescription(productName, featureDescriptions);
            product.setDescription(description);
            modelService.save(product);
        } catch (Exception e) {
            LOG.error("Error generating product description.", e);
        }
    }
    private List<Feature> filterFeaturesWithValues(FeatureList featuresList) {
        return featuresList.getFeatures().stream()
                .filter(feature -> !feature.getValues().isEmpty())
                .collect(Collectors.toList());
    }
    private Map<String, List<String>> generateFeatureDescriptions(List<Feature> features) {
        Map<String, List<String>> featureDescriptions = new ConcurrentHashMap<>();
        features.forEach(feature -> {
            List<String> valueDescriptions = feature.getValues().stream()
                    .map(value -> value.getValue().toString())
                    .collect(Collectors.toList());
            featureDescriptions.put(feature.getName(), valueDescriptions);
        });
        return featureDescriptions;
    }
    @Override
    public String generateProductDescription(String productName, Map<String, List<String>> featureDescriptions) throws MalformedURLException, HttpClientException, GenerationException {
        StringBuilder featureDescStringBuilder = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : featureDescriptions.entrySet()) {
            for (String value : entry.getValue()) {
                featureDescStringBuilder.append(entry.getKey())
                        .append(" : ")
                        .append(value)
                        .append(", ");
            }
        }
        return generateDescriptionFromAPI(productName, featureDescStringBuilder.toString());
    }
    @Override
    public String generateDescriptionFromAPI(String productName, String features) throws GenerationException, HttpClientException, MalformedURLException {
        String prompt = createPrompt(productName, features);
        JsonObject body = httpClientService.createRequestBody(prompt, 100);

        URL url = new URL(this.API_URL);
        HttpURLConnection httpURLConnection = httpClientService.createConnection(url);
        httpClientService.sendRequest(httpURLConnection, body);

        return parseDescripitonTextFromResponse(httpURLConnection, prompt);
    }

    private String createPrompt(String productName, String features) {
        return "Generated a description of this product " + productName + "\n\n based on this Features: " + features + "\n\n";
    }
    private String parseDescripitonTextFromResponse(HttpURLConnection httpURLConnection, String prompt) throws GenerationException {
        try {
            return httpClientService.extractResponseText(httpURLConnection, prompt);
        } catch (Exception e) {
            throw new GenerationException("Failed to parse translated text from response.", e);
        }
    }
}

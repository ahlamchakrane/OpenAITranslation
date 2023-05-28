package com.sqli.descriptionsTranslation.service.impl;
import com.sqli.descriptionsHttpClient.service.HttpClientService;
import com.sqli.descriptionsTranslation.service.ProductTranslationService;
import com.sqli.exceptions.GenerationException;
import com.sqli.exceptions.HttpClientException;
import com.sqli.service.impl.DefaultOpenAIAutoDescriptionGeneratorService;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.json.JSONException;
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
import java.util.stream.Collectors;

@Service
public class ProductTranslationServiceImpl implements ProductTranslationService {
    @Value("${OpenAI.API_URL}")
    private String API_URL;
    private final FlexibleSearchService flexibleSearchService;
    private final ModelService modelService;
    private final HttpClientService httpClientService;
    private final Map<String, Locale> languageLocaleMap;
    private static final Logger LOG = LoggerFactory.getLogger(DefaultOpenAIAutoDescriptionGeneratorService.class);

    public ProductTranslationServiceImpl(FlexibleSearchService flexibleSearchService, ModelService modelService, HttpClientService httpClientService) {
        this.flexibleSearchService = flexibleSearchService;
        this.modelService = modelService;
        this.httpClientService = httpClientService;
        this.languageLocaleMap = new HashMap<>();
    }
    @Override
    public List<ProductModel> getAllProductsNeedsTranslation() {
        List<ProductModel> products = getAllProducts();
        List<String> languages = getAllLanguages();

        return products.stream()
                .filter(product -> requiresTranslation(product, languages))
                .collect(Collectors.toList());
    }
    @Override
    public List<ProductModel> getAllProducts() {
        String query = "SELECT DISTINCT {p:pk} FROM {Product AS p JOIN CatalogVersion AS cv ON {p.catalogVersion}={cv.pk}} WHERE {cv.version}='Staged'";
        return performSearch(query, Collections.emptyMap());
    }
    @Override
    public List<ProductModel> getProductById(String productId) {
        String query = "SELECT DISTINCT {p:pk} FROM {Product AS p JOIN CatalogVersion AS cv ON {p.catalogVersion}={cv.pk}} WHERE {cv.version}='Staged' and {p.code}=?productId";
        Map<String, Object> params = Collections.singletonMap("productId", (Object) productId);
        List<ProductModel> products = performSearch(query, params);
        return !products.isEmpty() ? products : null; // or throw an exception if desired
    }

    @Override
    public List<String> getAllLanguages() {
        String query = "SELECT DISTINCT {l:pk} FROM {Language AS l}";
        List<LanguageModel> languages = performSearch(query, Collections.emptyMap());
        populateLanguageLocaleMap(languages);
        return languages.stream()
                .map(LanguageModel::getIsocode)
                .collect(Collectors.toList());
    }

    private boolean requiresTranslation(ProductModel product, List<String> languages) {
        boolean needsTranslation = false;
        boolean hasDescription = false;

        for (String language : languages) {
            Locale locale = languageLocaleMap.get(language);
            String description = product.getDescription(locale);

            if (description != null && !description.trim().isEmpty()) {
                hasDescription = true;
            } else {
                needsTranslation = true;
            }
        }

        return needsTranslation && hasDescription;
    }

    private void populateLanguageLocaleMap(List<LanguageModel> languages) {
        for (LanguageModel language : languages) {
            String languageCode = language.getIsocode();
            Locale locale = getLocaleFromLanguage(languageCode);
            languageLocaleMap.put(languageCode, locale);
        }
    }
    private Locale getLocaleFromLanguage(String language) {
        String[] parts = language.split("_");
        String languagePart = parts[0].toLowerCase();
        String countryPart = (parts.length > 1) ? parts[1].toUpperCase() : "";
        return new Locale(languagePart, countryPart);
    }
    @Override
    public List<String> getAllAvailableLanguages() {
        return new ArrayList<>(languageLocaleMap.keySet());
    }

    private <T> List<T> performSearch(String query, Map<String, Object> params) {
        FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query);
        params.forEach(searchQuery::addQueryParameter);
        SearchResult<T> searchResult = flexibleSearchService.search(searchQuery);
        return searchResult.getResult();
    }

    @Override
    public void translateProductDescriptions(ProductModel product, List<String> languages)
            throws MalformedURLException, HttpClientException, GenerationException, JSONException {
        String sourceDescription = findSourceDescription(product, languages);
        if (sourceDescription == null) {
            LOG.error("No description available for product: {}. Translation cannot be performed.", product.getCode());
            return;
        }
        List<String> targetLanguages = getTargetLanguages(product, languages);
        if (targetLanguages.isEmpty()) return;

        Map<String, String> translatedDescriptions = generateTranslations(sourceDescription, targetLanguages);
        saveTranslatedDescriptions(product, translatedDescriptions);
    }



    private String findSourceDescription(ProductModel product, List<String> languages) {
        String sourceDescription = null;

        for (String language : languages) {
            Locale locale = this.languageLocaleMap.get(language);
            String description = product.getDescription(locale);
            if (description != null && !description.trim().isEmpty()) {
                sourceDescription = description;
                break;
            }
        }

        return sourceDescription;
    }
    //method that return if a product description in a specific language is empty, if so it will be added in targetlanguages table . then we return this table
    private List<String> getTargetLanguages(ProductModel product, List<String> languages) {
        List<String> targetLanguages = new ArrayList<>();

        for (String language : languages) {
            Locale locale = this.languageLocaleMap.get(language);
            if (isDescriptionEmpty(product, locale)) targetLanguages.add(language);
        }
        return targetLanguages;
    }

    private boolean isDescriptionEmpty(ProductModel product, Locale locale) {
        String description = product.getDescription(locale);
        return description == null || description.trim().isEmpty();
    }
    @Override
    public Map<String, String> generateTranslations(String description, List<String> targetLanguages) throws HttpClientException, GenerationException, MalformedURLException, JSONException {
        LOG.info("Generating translations for description: {}", description);
        Map<String, String> translations = new HashMap<>();

        for (String language : targetLanguages) {
            String prompt = createPrompt(description, language);
            int maxTokens = Math.max(50, 2 * description.length());
            JsonObject requestBody = httpClientService.createRequestBody(prompt, maxTokens);

            URL url = new URL(this.API_URL);
            HttpURLConnection httpURLConnection = httpClientService.createConnection(url);
            httpClientService.sendRequest(httpURLConnection, requestBody);

            String translatedText = httpClientService.extractResponseText(httpURLConnection, prompt);

            translations.put(language, translatedText);
        }

        return translations;
    }

    private String createPrompt(String description, String targetLanguages) {
        return "Translate this " + description + " to " +targetLanguages + " :\n";
    }

    private void saveTranslatedDescription(ProductModel product, String language, String translatedDescription) {
        Locale locale = this.languageLocaleMap.get(language);
        product.setDescription(translatedDescription, locale);
        modelService.save(product);
    }

    private void saveTranslatedDescriptions(ProductModel product, Map<String, String> translatedDescriptions) {
        for (Map.Entry<String, String> entry : translatedDescriptions.entrySet()) { //save product description in multiple languages
            saveTranslatedDescription(product, entry.getKey(), entry.getValue());
        }
    }
    @Override
    public Map<String, String> processGenerationTranslation(String description, List<String> targetLanguages) throws HttpClientException, GenerationException, MalformedURLException, JSONException {
        LOG.info("Generating translations for description: {}", description);
        Map<String, String> translations = new HashMap<>();

        // Token count for each translation task. This is an approximation and may need to be adjusted.
        int tokensPerTranslation = Math.max(50, 3 * description.length()) + "Translate to language: ".length() + "<|end-of-translation|>".length();

        // Split the target languages into batches that fit within the token limit
        List<List<String>> batches = new ArrayList<>();
        List<String> currentBatch = new ArrayList<>();
        int tokensForCurrentBatch = 0;

        for (String targetLanguage : targetLanguages) {
            if (tokensForCurrentBatch + tokensPerTranslation > 4096) {
                batches.add(currentBatch);
                currentBatch = new ArrayList<>();
                tokensForCurrentBatch = 0;
            }

            currentBatch.add(targetLanguage);
            tokensForCurrentBatch += tokensPerTranslation;
        }

        if (!currentBatch.isEmpty()) {
            batches.add(currentBatch);
        }

        // Send a separate request for each batch
        for (List<String> batch : batches) {
            StringBuilder promptBuilder = new StringBuilder();
            for (String targetLanguage : batch) {
                promptBuilder.append("Translate to ").append(targetLanguage).append(": ").append(description).append("\n<|end-of-translation|>\n");
            }

            String prompt = promptBuilder.toString();
            JsonObject requestBody = httpClientService.createRequestBody(prompt, tokensForCurrentBatch);

            URL url = new URL(this.API_URL);
            HttpURLConnection httpURLConnection = httpClientService.createConnection(url);
            httpClientService.sendRequest(httpURLConnection, requestBody);

            String response = httpClientService.extractResponseText(httpURLConnection, prompt);
            String[] translatedTexts = response.split("<|end-of-translation|>");

            for (int i = 0; i < batch.size(); i++) {
                String translatedText = translatedTexts[i].trim();
                translations.put(batch.get(i), translatedText);
            }
        }

        return translations;
    }


    /*
    private Map<String, String> generateTranslationsFaster(String description, List<String> targetLanguages) throws HttpClientException, GenerationException, MalformedURLException {
        LOG.info("Generating translations for description: {}", description);
        String prompt = createPrompt(targetLanguages);
        int maxTokens = Math.max(50, 2 * description.length());
        JsonObject requestBody = createRequestBody(prompt, maxTokens, targetLanguages.size());

        URL url = new URL(this.API_URL);
        HttpURLConnection httpURLConnection = httpClientService.createConnection(url);
        httpClientService.sendRequest(httpURLConnection, requestBody);

        return parseTranslatedTextsFromResponse(httpURLConnection, prompt);
    }*/
}
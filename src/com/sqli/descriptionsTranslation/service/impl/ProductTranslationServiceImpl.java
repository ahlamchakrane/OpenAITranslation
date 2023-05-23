package com.sqli.descriptionsTranslation.service.impl;

import com.sqli.descriptionsHttpClient.service.HttpClientService;
import com.sqli.descriptionsTranslation.service.ProductTranslationService;
import com.sqli.service.impl.DefaultOpenAIAutoDescriptionGeneratorService;
import de.hybris.platform.catalog.model.ProductFeatureModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class ProductTranslationServiceImpl implements ProductTranslationService {
    @Value("${OpenAI.API_KEY}")
    private String API_KEY;
    @Value("${OpenAI.API_URL}")
    private String API_URL;
    private FlexibleSearchService flexibleSearchService;
    private ModelService modelService;
    private HttpClientService httpClientService;
    private static final Logger LOG = LoggerFactory.getLogger(DefaultOpenAIAutoDescriptionGeneratorService.class);

    public ProductTranslationServiceImpl(FlexibleSearchService flexibleSearchService, ModelService modelService, HttpClientService httpClientService) {
        this.flexibleSearchService = flexibleSearchService;
        this.modelService = modelService;
        this.httpClientService = httpClientService;
    }

    @Override
    public List<ProductModel> getAllProducts() {
        // String query = "SELECT DISTINCT {p:pk} FROM {Product AS p} WHERE {p:description} is not null";
        String query = "SELECT DISTINCT {p:pk} FROM {Product AS p} WHERE {p:code} = ?code";
        FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query);
        searchQuery.addQueryParameter("code", 300441142);
        SearchResult<ProductModel> searchResult = flexibleSearchService.search(searchQuery);
        return searchResult.getResult();
    }

    @Override
    public List<String> getLanguages() {
        String query = "SELECT DISTINCT {l:pk} FROM {Language AS l}";
        FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query);
        SearchResult<LanguageModel> searchResult = flexibleSearchService.search(searchQuery);
        Set<String> languageIsocodes = new HashSet<>();
        for (LanguageModel languageModel : searchResult.getResult()) {
            languageIsocodes.add(languageModel.getIsocode());
        }
        List<String> languageIsocodeList = new ArrayList<>(languageIsocodes);
        return languageIsocodeList;
    }

    @Override
    public String createPrompt(String targetLanguage) {
        String prompt = "Translate to " + targetLanguage + " :\n";
        return prompt;
    }

    public JsonObject createRequestBody(String prompt, String description) {
        return Json.createObjectBuilder()
                .add("prompt", prompt + "the following text " + description)
                .add("max_tokens", 100)
                .add("temperature", 0)
                .add("n", 1)
                .build();
    }

    @Override
    public String generateTranslation(String description, String targetLanguage) {
        try {
            String prompt = createPrompt(targetLanguage);
            // Create the request body with the prompt, text, and target language using JSON objects
            JsonObject requestBody = createRequestBody(prompt, description);
            URL url = new URL(this.API_URL);
            HttpURLConnection httpURLConnection = this.httpClientService.createConnection(url);
            // Send the request with the request body
            this.httpClientService.sendRequest(httpURLConnection, requestBody);
            // Get the response from the API
            String response = this.httpClientService.getResponse(httpURLConnection);
            // Parse the response JSON and retrieve the translated text
            String translatedText = parseTranslatedTextFromResponse(response);
            return translatedText;
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return description; // Return the original text if the translation fails
    }

    @Override
    public String parseTranslatedTextFromResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray choicesArray = jsonResponse.getJSONArray("choices");
            String text = choicesArray.getJSONObject(0).getString("text");
            text = text.trim().replace(response, "");
            return text;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public void saveTranslatedDescription(ProductModel product, String language, String translatedDescription) {
        Locale locale = new Locale(language.toLowerCase(), language.toUpperCase());
        product.setDescription(translatedDescription, locale);
        modelService.save(product);
    }
}
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
        //String query = "SELECT DISTINCT {p:pk} FROM {Product AS p} WHERE {p:description} is not null";
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
        return searchResult.getResult().stream()
                .map(LanguageModel::getIsocode)
                .collect(Collectors.toList());
    }

    @Override
    public String createPrompt(List<String> targetLanguages) {
        return "Translate to " + String.join(", ", targetLanguages) + " :\n";
    }

    private JsonObject createRequestBody(String prompt, String description,int maxTokens, int languageCount) {
        return Json.createObjectBuilder()
                .add("prompt", prompt + "the following text " + description)
                .add("max_tokens", maxTokens * (languageCount + 1))
                .add("temperature", 0)
                .add("n", 1)
                .build();
    }

    @Override
    public Map<String, String> generateTranslations(String description, List<String> targetLanguages) throws HttpClientException, GenerationException, MalformedURLException {
        String prompt = createPrompt(targetLanguages);
        int maxTokens = Math.max(50, 2 * description.length());
        JsonObject requestBody = createRequestBody(prompt, description, maxTokens,  targetLanguages.size());

        URL url = new URL(this.API_URL);
        HttpURLConnection httpURLConnection = httpClientService.createConnection(url);
        httpClientService.sendRequest(httpURLConnection, requestBody);
        String response = httpClientService.getResponse(httpURLConnection);

        return httpClientService.parseTranslatedTextsFromResponse(response, prompt);
    }
    @Override
    public void saveTranslatedDescription(ProductModel product, String language, String translatedDescription) {
        Locale locale = Locale.forLanguageTag(language.replace('_', '-')); // replace '_' with '-' to conform to BCP 47 language tags so the language "zh_TW" will be correctly interpreted as "zh_TW" rather than "zh_tw"
        product.setDescription(translatedDescription, locale);
        modelService.save(product);
    }
}
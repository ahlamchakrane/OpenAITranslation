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
    private String API_KEY ;
    @Value("${OpenAI.API_URL}")
    private String API_URL;
    private FlexibleSearchService flexibleSearchService;
    private ModelService modelService;
    private static final Logger LOG = LoggerFactory.getLogger(DefaultOpenAIAutoDescriptionGeneratorService.class);

    public ProductTranslationServiceImpl(FlexibleSearchService flexibleSearchService, ModelService modelService) {
        this.flexibleSearchService = flexibleSearchService;
        this.modelService = modelService;
    }
        @Override
        public List<ProductModel> getAllProducts() {
            String query = "SELECT DISTINCT {p:pk} FROM {Product AS p} WHERE {p:code} = ?code";
            FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query);
            searchQuery.addQueryParameter("code", 300441142);
            SearchResult<ProductModel> searchResult = flexibleSearchService.search(searchQuery);
            return searchResult.getResult();
        }
    @Override
    public List<LanguageModel> getLanguagesFromProductFeatures(ProductModel product) {
        Set<LanguageModel> languageSet = new HashSet<>();
        List<ProductFeatureModel> features = product.getFeatures();
        for (ProductFeatureModel feature : features) {
            LanguageModel language = feature.getLanguage();
            if (language != null) {
                languageSet.add(language);
            }
        }
        return new ArrayList<>(languageSet);
    }
        @Override
    public String createPrompt(String targetLanguage) {
        String prompt = "Translate the following text to " + targetLanguage +" :\n";
        return prompt;
    }
    public JsonObject createRequestBody(String prompt, String description) {
        return Json.createObjectBuilder()
                .add("prompt", prompt)
                .add("text", description)
                .build();
    }
    @Override
    public String generateTranslation(String description, String targetLanguage) {
        try {
            String prompt = createPrompt(targetLanguage);
            // Create the request body with the prompt, text, and target language using JSON objects
            JsonObject requestBody = createRequestBody(prompt, description);
            URL url = new URL(this.API_URL);
            HttpURLConnection httpURLConnection = createConnection(url);
            // Send the request with the request body
            sendRequest(httpURLConnection, requestBody);
            // Get the response from the API
            String response = getResponse(httpURLConnection);
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
            JsonObject jsonResponse = Json.createReader(new StringReader(response)).readObject();
            String translatedText = jsonResponse.getString("translated_text");
            return translatedText;
    }
    @Override
    public void saveTranslatedDescription(ProductModel product, LanguageModel language, String translatedDescription) {
            Locale locale = new Locale(language.getIsocode(), language.getIsocode().toUpperCase());
            product.setDescription(translatedDescription, locale);
            modelService.save(product);
        }
    @Override
    public HttpURLConnection createConnection(URL url) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + this.API_KEY);
        httpURLConnection.setDoOutput(true);
        return httpURLConnection;
    }
    @Override
    public  void sendRequest(HttpURLConnection httpURLConnection, JsonObject body) throws Exception {
        httpURLConnection.getOutputStream().write(body.toString().getBytes());
        httpURLConnection.getOutputStream().flush();
        httpURLConnection.getOutputStream().close();
    }
    @Override
    public  String getResponse(HttpURLConnection httpURLConnection) throws Exception {
        StringBuilder response = new StringBuilder();
        try (InputStream is = httpURLConnection.getInputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                response.append(new String(buffer, 0, bytesRead));
            }
        }
        return response.toString();
    }
}
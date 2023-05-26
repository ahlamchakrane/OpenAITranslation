package com.sqli.descriptionsTranslation.jobs;
import com.sqli.descriptionsTranslation.service.ProductTranslationService;
import com.sqli.exceptions.GenerationException;
import com.sqli.exceptions.HttpClientException;
import com.sqli.model.ProductDescriptionTranslationCronJobModel;
import com.sqli.service.impl.DefaultOpenAIAutoDescriptionGeneratorService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductDescriptionTranslationJob extends AbstractJobPerformable<ProductDescriptionTranslationCronJobModel> {
    private ProductTranslationService productTranslationService;
    private static final Logger LOG = LoggerFactory.getLogger(DefaultOpenAIAutoDescriptionGeneratorService.class);

    public ProductDescriptionTranslationJob(ProductTranslationService productTranslationService) {
        this.productTranslationService = productTranslationService;
    }
    @Override
    public PerformResult perform(final ProductDescriptionTranslationCronJobModel args0) {
        try {
            processTranstalions();
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        } catch (HttpClientException | GenerationException e) {
            LOG.error("Failed to generate translation.", e);
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        } catch (Exception e) {
            LOG.error("Unexpected error occurred.", e);
            throw new RuntimeException("Unexpected error occurred while performing job.", e);
        }
    }

    private void processTranstalions() throws MalformedURLException, HttpClientException, GenerationException {
        List<ProductModel> products = productTranslationService.getAllProducts();
        List<String> languages = productTranslationService.getLanguages();

        for (ProductModel product : products) { // for each product we translate it to languages
            translateProductDescriptions(product, languages);
        }
    }

    private void translateProductDescriptions(ProductModel product, List<String> languages) throws MalformedURLException, HttpClientException, GenerationException {
        List<String> targetLanguages = getTargetLanguages(product, languages); //verify if product in a particular language do not have description
        if (targetLanguages.isEmpty()) return;
        Map<String, String> translatedDescriptions = productTranslationService.generateTranslations(product.getDescription(), targetLanguages);
        saveTranslatedDescriptions(product, translatedDescriptions);
    }

    private void saveTranslatedDescriptions(ProductModel product, Map<String, String> translatedDescriptions) {
        for (Map.Entry<String, String> entry : translatedDescriptions.entrySet()) { //in each language (key) we save it's description (value)
            productTranslationService.saveTranslatedDescription(product, entry.getKey(), entry.getValue());
        }
    }

    private List<String> getTargetLanguages(ProductModel product, List<String> languages) {
        List<String> targetLanguages = new ArrayList<>();
        for (String language : languages) {
            Locale locale = Locale.forLanguageTag(language.replace('_', '-')); // replace '_' with '-' to conform to BCP 47 language tags so the language "zh_TW" will be correctly interpreted as "zh_TW" rather than "zh_tw"
            if (isDescriptionEmpty(product, locale)) targetLanguages.add(language); //if description in this language is empty, so we have to generate it's translation
        }
            return targetLanguages;
    }
    /*

    private Locale getLocaleFromLanguage(String language) {
    String[] parts = language.split("_");
    String languagePart = parts[0].toLowerCase();
    String countryPart = (parts.length > 1) ? parts[1].toUpperCase() : "";
    return new Locale(languagePart, countryPart);
    }
     */

    private boolean isDescriptionEmpty(ProductModel product, Locale locale) {
        String description = product.getDescription(locale);
        return description == null || description.trim().isEmpty();
    }
}







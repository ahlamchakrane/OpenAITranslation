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
import org.json.JSONException;
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
    private String productId;
    public ProductDescriptionTranslationJob(ProductTranslationService productTranslationService) {
        this.productTranslationService = productTranslationService;
    }
    @Override
    public PerformResult perform(final ProductDescriptionTranslationCronJobModel cronJobModel) {
        this.productId = cronJobModel.getProduct_ID();
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

    private void processTranstalions() throws MalformedURLException, HttpClientException, GenerationException, JSONException {
        List<ProductModel> products;
        List<String> languages;
        if (this.productId == null || this.productId.trim().isEmpty()) {
            products = productTranslationService.getAllProductsNeedsTranslation();
            languages = productTranslationService.getAllAvailableLanguages();
        } else {
            products = productTranslationService.getProductById(this.productId);
            languages = productTranslationService.getAllLanguages();
        }
        productTranslationService.translateProductDescriptions(products.get(0),languages);
    }
}







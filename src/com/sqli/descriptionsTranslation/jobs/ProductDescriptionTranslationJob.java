package com.sqli.descriptionsTranslation.jobs;
import com.sqli.descriptionsTranslation.service.ProductTranslationService;
import com.sqli.model.ProductDescriptionTranslationCronJobModel;
import com.sqli.service.impl.DefaultOpenAIAutoDescriptionGeneratorService;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ProductDescriptionTranslationJob extends AbstractJobPerformable<ProductDescriptionTranslationCronJobModel> {
    private ProductTranslationService productTranslationService;
    private static final Logger LOG = LoggerFactory.getLogger(DefaultOpenAIAutoDescriptionGeneratorService.class);

    public ProductDescriptionTranslationJob(ProductTranslationService productTranslationService) {
        this.productTranslationService = productTranslationService;
    }

    @Override
    public PerformResult perform(final ProductDescriptionTranslationCronJobModel args0) {
        try {
            // Retrieve all products with non-empty descriptions
            List<ProductModel> products = this.productTranslationService.getAllProducts();
            for (ProductModel product : products) {
                // Get the languages from the product features
                List<LanguageModel> languages = this.productTranslationService.getLanguagesFromProductFeatures(product);
                // Generate translations for each language
                for (LanguageModel language : languages) {
                    Locale locale = new Locale(language.getIsocode(), language.getIsocode().toUpperCase());

                    if (product.getDescription(locale).isEmpty()) {
                        System.out.println("yes empty");
                        String translatedDescription = this.productTranslationService.generateTranslation(product.getDescription(), language.getIsocode());
                        this.productTranslationService.saveTranslatedDescription(product, language, translatedDescription);
                    }
                }
            }
        } catch (Exception e) {
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }
}







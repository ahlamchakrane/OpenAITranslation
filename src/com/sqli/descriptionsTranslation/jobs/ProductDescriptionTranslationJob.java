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

import java.util.ArrayList;
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
            List<String> languages = this.productTranslationService.getLanguages();
            for (ProductModel product : products) {
                // Generate translations for each language
                for (int i = 0 ; i< 3; i++) {
                   Locale locale = new Locale(languages.get(i).toLowerCase(), languages.get(i).toUpperCase());
                   if(!languages.get(i).equals("en") || !languages.get(i).equals("fr")){
                       System.out.println("not en or fr"+ languages.get(i));
                       if (product.getDescription(locale) == null || product.getDescription(locale).isEmpty() || product.getDescription(locale).isBlank() || product.getDescription(locale).equals("")) {
                           String translatedDescription = this.productTranslationService.generateTranslation(product.getDescription(), languages.get(i));
                           this.productTranslationService.saveTranslatedDescription(product, languages.get(i), translatedDescription);
                       }
                   }

                }
                return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
            }
        } catch (Exception e) {
            e.getMessage();
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }
}







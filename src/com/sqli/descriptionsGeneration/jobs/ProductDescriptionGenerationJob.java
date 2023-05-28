package com.sqli.descriptionsGeneration.jobs;
import com.sqli.model.ProductDescriptionGenerationCronJobModel;
import com.sqli.descriptionsGeneration.service.ProductDescriptionService;
import com.sqli.service.impl.DefaultOpenAIAutoDescriptionGeneratorService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductDescriptionGenerationJob extends AbstractJobPerformable<ProductDescriptionGenerationCronJobModel>  {
    private ProductDescriptionService productDescriptionService;
    private static final Logger LOG = LoggerFactory.getLogger(DefaultOpenAIAutoDescriptionGeneratorService.class);
    public ProductDescriptionGenerationJob(ProductDescriptionService productDescriptionService) {
        this.productDescriptionService = productDescriptionService;
    }
   @Override
    public PerformResult perform(final ProductDescriptionGenerationCronJobModel cronJobModel)  {
       List<ProductModel> products;
       String productId = cronJobModel.getProduct_ID();
       if (productId == null || productId.trim().isEmpty()) {
           products = productDescriptionService.getProductsWithoutDescription();
       } else {
           products = productDescriptionService.getProductById(productId);
       }
       products.stream()
               .filter(product -> {
                   if (!productDescriptionService.hasNonEmptyFeatures(product)) {
                       LOG.info("Product {} has empty features", product.getCode());
                       return false;
                   }
                   return true;
               })
               .findFirst()
               .ifPresent(product -> {
                   LOG.info("Processing product {}", product.getCode());
                   productDescriptionService.processGenerationDescription(product);
               });
       return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
   }
}


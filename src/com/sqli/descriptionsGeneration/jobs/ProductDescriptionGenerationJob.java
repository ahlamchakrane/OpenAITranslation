package com.sqli.descriptionsGeneration.jobs;

import com.sqli.model.ProductDescriptionGenerationCronJobModel;
import com.sqli.descriptionsGeneration.service.ProductDescriptionService;
import com.sqli.service.impl.DefaultOpenAIAutoDescriptionGeneratorService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
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
    public PerformResult perform(final ProductDescriptionGenerationCronJobModel args0)  {
           try {
              List<ProductModel> products = getProductsWithoutDescription();
              for(ProductModel product : products) {
                  String productName = product.getName();
                  LOG.info("Product ID: {}", product.getCode()); // Modified log statement
                  String features = String.valueOf(product.getFeatures());
                  String description = productDescriptionService.generateProductDescription(productName, features);
                  LOG.info("Product description: {}"+ description);
                  product.setDescription(description);
                  modelService.save(product);
                  return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
              }
           } catch (Exception e) {
               return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
           }
       return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
   }

       public List<ProductModel> getProductsWithoutDescription() {
           String query = "SELECT {p:pk} FROM {Product AS p} WHERE {p:description} IS NULL and {p:name} IS NOT NULL";
           FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query);
           SearchResult<ProductModel> searchResult = flexibleSearchService.search(searchQuery);
           return searchResult.getResult();
       }
    }


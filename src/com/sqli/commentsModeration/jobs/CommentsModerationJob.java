package com.sqli.commentsModeration.jobs;

import com.sqli.commentsModeration.service.CommentsModerationService;
import com.sqli.descriptionsGeneration.service.ProductDescriptionService;
import com.sqli.exceptions.GenerationException;
import com.sqli.exceptions.HttpClientException;
import com.sqli.model.CommentsModerationCronJobModel;
import com.sqli.service.impl.DefaultOpenAIAutoDescriptionGeneratorService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.customerreview.model.CustomerReviewModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.List;

public class CommentsModerationJob extends AbstractJobPerformable<CommentsModerationCronJobModel> {
    private CommentsModerationService commentsModerationService;
    private static final Logger LOG = LoggerFactory.getLogger(DefaultOpenAIAutoDescriptionGeneratorService.class);
    public CommentsModerationJob(CommentsModerationService commentsModerationService) {
        this.commentsModerationService = commentsModerationService;
    }
    @Override
    public PerformResult perform(final CommentsModerationCronJobModel cronJobModel) {
        List<CustomerReviewModel> reviews;
        String reviewId = cronJobModel.getReview_ID();

        if (reviewId.equals(String.valueOf(-1)) || reviewId.trim().isEmpty()) {
            reviews = commentsModerationService.getAllReviews();
        } else {
            reviews = commentsModerationService.getReviewById(reviewId);
        }
        if(reviews != null) {
            try {
                commentsModerationService.filterReviews(reviews);
            } catch (MalformedURLException e ) {
                throw new RuntimeException(e);
            } catch (HttpClientException e) {
                throw new RuntimeException(e);
            } catch (GenerationException e) {
                throw new RuntimeException(e);
            }
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        }
        return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
    }
}

package com.sqli.commentsModeration.service;

import com.sqli.exceptions.GenerationException;
import com.sqli.exceptions.HttpClientException;
import de.hybris.platform.customerreview.model.CustomerReviewModel;

import java.net.MalformedURLException;
import java.util.List;

public interface CommentsModerationService {
    List<CustomerReviewModel> getAllReviews();
    List<CustomerReviewModel> getReviewById(String reviewId);
    void filterReviews(List<CustomerReviewModel> reviews) throws MalformedURLException, HttpClientException, GenerationException;
    double analyzeContentFromAPI(String comment) throws HttpClientException, GenerationException, MalformedURLException;

}

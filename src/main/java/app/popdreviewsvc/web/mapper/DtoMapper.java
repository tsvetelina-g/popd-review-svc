package app.popdreviewsvc.web.mapper;

import app.popdreviewsvc.model.Review;
import app.popdreviewsvc.web.dto.MovieReviewStatsResponse;
import app.popdreviewsvc.web.dto.ReviewResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static ReviewResponse from(Review review) {

        return ReviewResponse.builder()
                .movieId(review.getMovieId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .createdOn(review.getCreatedOn())
                .updatedOn(review.getUpdatedOn())
                .build();
    }

    public static MovieReviewStatsResponse from(Integer movieReviewsCount) {

        return MovieReviewStatsResponse.builder().totalReviews(movieReviewsCount).build();
    }


}

package app.popdreviewsvc.service;

import app.popdreviewsvc.model.Review;
import app.popdreviewsvc.repository.ReviewRepository;
import app.popdreviewsvc.web.dto.ReviewRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }


    public Review upsert(ReviewRequest reviewRequest) {

        Optional<Review> reviewOpt = reviewRepository.findByUserIdAndMovieId(reviewRequest.getUserId(), reviewRequest.getMovieId());

        if (reviewOpt.isPresent()){
            Review review = reviewOpt.get();

            review.setContent(reviewRequest.getContent());
            review.setTitle(reviewRequest.getTitle());
            review.setUpdatedOn(LocalDateTime.now());

            return reviewRepository.save(review);
        }

        Review review = Review.builder()
                .userId(reviewRequest.getUserId())
                .movieId(reviewRequest.getMovieId())
                .content(reviewRequest.getContent())
                .title(reviewRequest.getTitle())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return reviewRepository.save(review);
    }
}

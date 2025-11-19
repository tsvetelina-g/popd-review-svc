package app.popdreviewsvc.service;

import app.popdreviewsvc.model.Review;
import app.popdreviewsvc.repository.ReviewRepository;
import app.popdreviewsvc.web.dto.ReviewRequest;
import app.popdreviewsvc.web.dto.ReviewResponse;
import app.popdreviewsvc.web.mapper.DtoMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
            review.setRating(reviewRequest.getRating());
            review.setTitle(reviewRequest.getTitle());
            review.setUpdatedOn(LocalDateTime.now());

            return reviewRepository.save(review);
        }

        Review review = Review.builder()
                .userId(reviewRequest.getUserId())
                .movieId(reviewRequest.getMovieId())
                .rating(reviewRequest.getRating())
                .content(reviewRequest.getContent())
                .title(reviewRequest.getTitle())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return reviewRepository.save(review);
    }

    public Review findReviewByUserAndMovie(UUID userId, UUID movieId) {
        Optional<Review> reviewOpt = reviewRepository.findByUserIdAndMovieId(userId, movieId);

        return reviewOpt.orElse(null);
    }

    public Boolean removeReview(UUID userId, UUID movieId) {
        Optional<Review> reviewOpt = reviewRepository.findByUserIdAndMovieId(userId, movieId);

        if (reviewOpt.isPresent()) {
            reviewRepository.delete(reviewOpt.get());
            return true;
        }

        return false;
    }

    public List<ReviewResponse> getLatestFiveReviews(UUID movieId) {
        return reviewRepository.findAllByMovieIdOrderByUpdatedOnDesc(movieId).stream().limit(5).toList();
    }

    public Page<ReviewResponse> getReviewsForMovie(UUID movieId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByMovieIdOrderByUpdatedOnDesc(movieId, pageable);

        return reviews.map(DtoMapper::from);
    }

    public Integer getAllReviewsForAMovieCount(UUID movieId) {

        List<Review> reviews = reviewRepository.findAllByMovieId(movieId);

        if (reviews.isEmpty()) {
            return null;
        }

        return reviews.size();
    }
}

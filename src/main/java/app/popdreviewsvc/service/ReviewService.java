package app.popdreviewsvc.service;

import app.popdreviewsvc.exception.NotFoundException;
import app.popdreviewsvc.model.Review;
import app.popdreviewsvc.repository.ReviewRepository;
import app.popdreviewsvc.web.dto.ReviewRequest;
import app.popdreviewsvc.web.dto.ReviewResponse;
import app.popdreviewsvc.web.mapper.DtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Review upsert(ReviewRequest reviewRequest) {
        Optional<Review> reviewOpt = reviewRepository.findByUserIdAndMovieId(reviewRequest.getUserId(), reviewRequest.getMovieId());

        if (reviewOpt.isPresent()) {
            Review review = reviewOpt.get();

            review.setContent(reviewRequest.getContent());
            review.setRating(reviewRequest.getRating());
            review.setTitle(reviewRequest.getTitle());
            review.setUpdatedOn(LocalDateTime.now());

            Review savedReview = reviewRepository.save(review);
            log.info("Successfully updated review with id {} for user with id {} and movie with id {}",
                    savedReview.getId(), savedReview.getUserId(), savedReview.getMovieId());
            return savedReview;
        }

        Review review = Review.builder()
                .id(UUID.randomUUID())
                .userId(reviewRequest.getUserId())
                .movieId(reviewRequest.getMovieId())
                .rating(reviewRequest.getRating())
                .content(reviewRequest.getContent())
                .title(reviewRequest.getTitle())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Successfully created new review with id {} for user with id {} and movie with id {}",
                savedReview.getId(), savedReview.getUserId(), savedReview.getMovieId());
        return savedReview;
    }

    public Review findByUserIdAndMovieId(UUID userId, UUID movieId) {
        return reviewRepository.findByUserIdAndMovieId(userId, movieId).orElseThrow(() -> new NotFoundException("Review with user id [%s] and movie id [%s] not found".formatted(userId, movieId)));
    }

    public void removeReview(UUID userId, UUID movieId) {
        Review review = findByUserIdAndMovieId(userId, movieId);
        reviewRepository.delete(review);
        log.info("Successfully removed review with id {} for user with id {} and movie with id {}",
                review.getId(), userId, movieId);
    }

    public List<ReviewResponse> getLatestFiveReviews(UUID movieId) {
        List<Review> reviews = reviewRepository.findAllByMovieIdOrderByUpdatedOnDesc(movieId);

        if (reviews.isEmpty()) {
            throw new NotFoundException("Latest Reviews not found for movie with id [%s]".formatted(movieId));
        }

        return reviews.stream().map(DtoMapper::from).limit(5).toList();
    }

    public Page<ReviewResponse> getReviewsForMovie(UUID movieId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByMovieIdOrderByUpdatedOnDesc(movieId, pageable);

        return reviews.map(DtoMapper::from);
    }

    public Integer getAllReviewsForAMovieCount(UUID movieId) {
        List<Review> reviews = reviewRepository.findAllByMovieId(movieId);

        if (reviews.isEmpty()) {
            throw new NotFoundException("No reviews found for movie with id [%s]".formatted(movieId));
        }

        return reviews.size();
    }

    public Integer getAllReviewedMoviesCountByUser(UUID userId) {
        List<Review> reviews = reviewRepository.findAllByUserId(userId);

        if (reviews.isEmpty()) {
            throw new NotFoundException("No movies reviewed by user with id [%s]".formatted(userId));
        }

        return reviews.size();
    }

    public List<ReviewResponse> getLatestReviewsByUserId(UUID userId) {
        List<Review> latestReviews = reviewRepository.findAllByUserIdOrderByCreatedOnDesc(userId);

        if (latestReviews.isEmpty()) {
            throw new NotFoundException("Latest Reviews not found for user with id [%s]".formatted(userId));
        }

        List<ReviewResponse> responses = latestReviews.stream().map(DtoMapper::from).toList();

        return responses.stream().limit(20).toList();
    }
}

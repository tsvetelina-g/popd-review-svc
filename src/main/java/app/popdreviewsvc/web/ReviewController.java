package app.popdreviewsvc.web;

import app.popdreviewsvc.model.Review;
import app.popdreviewsvc.service.ReviewService;
import app.popdreviewsvc.web.dto.MovieReviewStatsResponse;
import app.popdreviewsvc.web.dto.ReviewRequest;
import app.popdreviewsvc.web.dto.ReviewResponse;
import app.popdreviewsvc.web.dto.UserReviewsStatsResponse;
import app.popdreviewsvc.web.mapper.DtoMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> upsertReview(@RequestBody ReviewRequest reviewRequest) {
        Review review = reviewService.upsert(reviewRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DtoMapper.from(review));
    }

    @GetMapping("/reviews/{userId}/{movieId}")
    public ResponseEntity<ReviewResponse> getReviewByUserAndMovie(@PathVariable UUID userId, @PathVariable UUID movieId) {
        Review review = reviewService.findByUserIdAndMovieId(userId, movieId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DtoMapper.from(review));
    }

    @DeleteMapping("/reviews/{userId}/{movieId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID userId, @PathVariable UUID movieId) {
        reviewService.removeReview(userId, movieId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reviews/{movieId}")
    public ResponseEntity<List<ReviewResponse>> getLatestFiveReviewsForAMovie(@PathVariable UUID movieId) {
        List<ReviewResponse> latestFiveReviews = reviewService.getLatestFiveReviews(movieId);

        return ResponseEntity.ok(latestFiveReviews);
    }

    @GetMapping("/reviews/{movieId}/page")
    public ResponseEntity<Page<ReviewResponse>> getReviewsForMovie(
            @PathVariable UUID movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(
                reviewService.getReviewsForMovie(movieId, PageRequest.of(page, size))
        );
    }

    @GetMapping("/reviews/{movieId}/stats")
    public ResponseEntity<MovieReviewStatsResponse> movieReviewsStats(@PathVariable UUID movieId) {
        Integer allReviewsCount = reviewService.getAllReviewsForAMovieCount(movieId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DtoMapper.fromMovieReviewsCount(allReviewsCount));
    }

    @GetMapping("/reviews/{userId}/user")
    public ResponseEntity<UserReviewsStatsResponse> userReviewsStats(@PathVariable UUID userId) {
        Integer moviesReviewedCount = reviewService.getAllReviewedMoviesCountByUser(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DtoMapper.fromUserReviewsCount(moviesReviewedCount));
    }

    @GetMapping("/reviews/{userId}/latest-reviews")
    public ResponseEntity<List<ReviewResponse>> latestReviewsByUser(@PathVariable UUID userId) {
        List<ReviewResponse> latestReviews = reviewService.getLatestReviewsByUserId(userId);

        return ResponseEntity.ok(latestReviews);
    }
}

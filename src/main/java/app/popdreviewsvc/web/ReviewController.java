package app.popdreviewsvc.web;

import app.popdreviewsvc.model.Review;
import app.popdreviewsvc.service.ReviewService;
import app.popdreviewsvc.web.dto.MovieReviewStatsResponse;
import app.popdreviewsvc.web.dto.ReviewRequest;
import app.popdreviewsvc.web.dto.ReviewResponse;
import app.popdreviewsvc.web.mapper.DtoMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
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

    @GetMapping("reviews/{userId}/{movieId}")
    public ResponseEntity<ReviewResponse> getReviewByUserAndMovie(@PathVariable UUID userId, @PathVariable UUID movieId) {

        Review review = reviewService.findReviewByUserAndMovie(userId, movieId);

        if (review == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DtoMapper.from(review));
    }

    @DeleteMapping("/reviews/{userId}/{movieId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID userId, @PathVariable UUID movieId) {

        Boolean isDeleted = reviewService.removeReview(userId, movieId);

        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("reviews/{movieId}")
    public ResponseEntity<List<ReviewResponse>> getLatestFiveReviewsForAMovie(@PathVariable UUID movieId) {

        List<ReviewResponse> latestFiveReviews = reviewService.getLatestFiveReviews(movieId);

        if (latestFiveReviews.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

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

        Integer allRatingsCount = reviewService.getAllReviewsForAMovieCount(movieId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DtoMapper.from(allRatingsCount));
    }
    
}

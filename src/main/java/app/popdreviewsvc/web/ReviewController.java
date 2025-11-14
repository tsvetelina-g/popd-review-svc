package app.popdreviewsvc.web;

import app.popdreviewsvc.model.Review;
import app.popdreviewsvc.service.ReviewService;
import app.popdreviewsvc.web.dto.ReviewRequest;
import app.popdreviewsvc.web.dto.ReviewResponse;
import app.popdreviewsvc.web.mapper.DtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
}

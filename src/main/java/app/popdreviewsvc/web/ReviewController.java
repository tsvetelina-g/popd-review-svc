package app.popdreviewsvc.web;

import app.popdreviewsvc.model.Review;
import app.popdreviewsvc.service.ReviewService;
import app.popdreviewsvc.web.dto.ReviewRequest;
import app.popdreviewsvc.web.dto.ReviewResponse;
import app.popdreviewsvc.web.mapper.DtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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

}

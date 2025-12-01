package app.popdreviewsvc;

import app.popdreviewsvc.model.Review;
import app.popdreviewsvc.repository.ReviewRepository;
import app.popdreviewsvc.service.ReviewService;
import app.popdreviewsvc.web.dto.ReviewRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UpsertReviewITest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @AfterEach
    void tearDown() {
        reviewRepository.deleteAll();
    }

    @Test
    void upsertReview_whenReviewDoesNotExist_shouldCreateNewReviewAndPersistInDatabase() {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        ReviewRequest reviewRequest = ReviewRequest.builder()
                .userId(userId)
                .movieId(movieId)
                .rating(5)
                .title("Title")
                .content("Content")
                .build();

        Review createdReview = reviewService.upsert(reviewRequest);

        assertNotNull(createdReview.getId());
        assertEquals(userId, createdReview.getUserId());
        assertEquals(movieId, createdReview.getMovieId());
        assertEquals(5, createdReview.getRating());
        assertEquals("Title", createdReview.getTitle());
        assertEquals("Content", createdReview.getContent());
        assertNotNull(createdReview.getCreatedOn());
        assertNotNull(createdReview.getUpdatedOn());

        Review reviewFromDb = reviewRepository.findById(createdReview.getId()).orElse(null);
        assertNotNull(reviewFromDb);
        assertEquals(5, reviewFromDb.getRating());
        assertEquals("Title", reviewFromDb.getTitle());
        assertEquals("Content", reviewFromDb.getContent());
        assertEquals(1, reviewRepository.count());
    }

    @Test
    void upsertReview_whenReviewAlreadyExists_shouldUpdateExistingReviewAndPersistInDatabase() {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        LocalDateTime originalCreatedOn = LocalDateTime.now().minusDays(1);

        Review existingReview = Review.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .movieId(movieId)
                .rating(3)
                .title("Old title")
                .content("Old content")
                .createdOn(originalCreatedOn)
                .updatedOn(originalCreatedOn)
                .build();
        reviewRepository.save(existingReview);

        ReviewRequest updateRequest = ReviewRequest.builder()
                .userId(userId)
                .movieId(movieId)
                .rating(5)
                .title("Updated title")
                .content("Updated content")
                .build();

        Review updatedReview = reviewService.upsert(updateRequest);

        assertEquals(existingReview.getId(), updatedReview.getId());
        assertEquals(5, updatedReview.getRating());
        assertEquals("Updated title", updatedReview.getTitle());
        assertEquals("Updated content", updatedReview.getContent());
        assertThat(updatedReview.getCreatedOn()).isCloseTo(originalCreatedOn, within(1, ChronoUnit.SECONDS));
        assertTrue(updatedReview.getUpdatedOn().isAfter(originalCreatedOn));
        assertEquals(1, reviewRepository.count());

        Review reviewFromDb = reviewRepository.findById(updatedReview.getId()).orElse(null);
        assertNotNull(reviewFromDb);
        assertEquals(5, reviewFromDb.getRating());
        assertEquals("Updated title", reviewFromDb.getTitle());
        assertEquals("Updated content", reviewFromDb.getContent());
    }

    @Test
    void upsertReview_multipleReviewsScenario_shouldHandleCorrectlyBasedOnUserAndMovie() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID movie1 = UUID.randomUUID();
        UUID movie2 = UUID.randomUUID();

        ReviewRequest user1Movie1 = ReviewRequest.builder()
                .userId(user1)
                .movieId(movie1)
                .rating(5)
                .title("User1 Movie1")
                .content("Content1")
                .build();
        ReviewRequest user1Movie2 = ReviewRequest.builder()
                .userId(user1)
                .movieId(movie2)
                .rating(4)
                .title("User1 Movie2")
                .content("Content2")
                .build();
        ReviewRequest user2Movie1 = ReviewRequest.builder()
                .userId(user2)
                .movieId(movie1)
                .rating(3)
                .title("User2 Movie1")
                .content("Content3")
                .build();

        reviewService.upsert(user1Movie1);
        reviewService.upsert(user1Movie2);
        reviewService.upsert(user2Movie1);

        assertEquals(3, reviewRepository.count());
        assertEquals(2, reviewRepository.findAllByUserId(user1).size());
        assertEquals(1, reviewRepository.findAllByUserId(user2).size());
        assertEquals(2, reviewRepository.findAllByMovieId(movie1).size());
        assertEquals(1, reviewRepository.findAllByMovieId(movie2).size());

        ReviewRequest user1Movie1Update = ReviewRequest.builder()
                .userId(user1)
                .movieId(movie1)
                .rating(1)
                .title("Updated User1 Movie1")
                .content("Updated Content1")
                .build();
        Review updated = reviewService.upsert(user1Movie1Update);

        assertEquals(3, reviewRepository.count());
        assertEquals(1, updated.getRating());
        assertEquals("Updated User1 Movie1", updated.getTitle());
        assertEquals("Updated Content1", updated.getContent());
    }
}

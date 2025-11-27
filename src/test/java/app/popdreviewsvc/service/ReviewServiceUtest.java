package app.popdreviewsvc.service;

import app.popdreviewsvc.exception.NotFoundException;
import app.popdreviewsvc.model.Review;
import app.popdreviewsvc.repository.ReviewRepository;
import app.popdreviewsvc.web.dto.ReviewRequest;
import app.popdreviewsvc.web.dto.ReviewResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceUTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void whenUpsert_andReviewDoesNotExist_thenCreateNewReviewAndPersist() {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        ReviewRequest request = ReviewRequest.builder()
                .userId(userId)
                .movieId(movieId)
                .rating(5)
                .title("Great movie!")
                .content("This is an amazing film.")
                .build();
        when(reviewRepository.findByUserIdAndMovieId(userId, movieId)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(UUID.randomUUID());
            return review;
        });

        Review result = reviewService.upsert(request);

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Great movie!", result.getTitle());
        assertEquals("This is an amazing film.", result.getContent());
        assertEquals(userId, result.getUserId());
        assertEquals(movieId, result.getMovieId());
        assertThat(result.getCreatedOn()).isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS));
        assertThat(result.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS));
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void whenUpsert_andReviewAlreadyExists_thenUpdateExistingReviewAndPersist() {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        ReviewRequest request = ReviewRequest.builder()
                .userId(userId)
                .movieId(movieId)
                .rating(4)
                .title("Updated title")
                .content("Updated content")
                .build();
        Review existingReview = Review.builder()
                .id(reviewId)
                .userId(userId)
                .movieId(movieId)
                .rating(3)
                .title("Old title")
                .content("Old content")
                .createdOn(LocalDateTime.now().minusDays(1))
                .updatedOn(LocalDateTime.now().minusDays(1))
                .build();
        when(reviewRepository.findByUserIdAndMovieId(userId, movieId)).thenReturn(Optional.of(existingReview));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Review result = reviewService.upsert(request);

        assertNotNull(result);
        assertEquals(reviewId, result.getId());
        assertEquals(4, result.getRating());
        assertEquals("Updated title", result.getTitle());
        assertEquals("Updated content", result.getContent());
        assertThat(result.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS));
        verify(reviewRepository).save(existingReview);
    }

    @Test
    void whenFindByUserIdAndMovieId_andReviewExists_thenReturnReview() {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        Review review = Review.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .movieId(movieId)
                .rating(5)
                .title("Great movie!")
                .content("Amazing film.")
                .build();
        when(reviewRepository.findByUserIdAndMovieId(userId, movieId)).thenReturn(Optional.of(review));

        Review result = reviewService.findByUserIdAndMovieId(userId, movieId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(movieId, result.getMovieId());
    }

    @Test
    void whenFindByUserIdAndMovieId_andReviewDoesNotExist_thenThrowException() {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        when(reviewRepository.findByUserIdAndMovieId(userId, movieId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reviewService.findByUserIdAndMovieId(userId, movieId));
    }

    @Test
    void whenRemoveReview_andReviewExists_thenDeleteReview() {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        Review review = Review.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .movieId(movieId)
                .rating(5)
                .title("Great movie!")
                .content("Amazing film.")
                .build();
        when(reviewRepository.findByUserIdAndMovieId(userId, movieId)).thenReturn(Optional.of(review));

        reviewService.removeReview(userId, movieId);

        verify(reviewRepository).delete(review);
    }

    @Test
    void whenRemoveReview_andReviewDoesNotExist_thenThrowException() {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        when(reviewRepository.findByUserIdAndMovieId(userId, movieId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reviewService.removeReview(userId, movieId));
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void whenGetLatestFiveReviews_andReviewsExist_thenReturnLatestFive() {
        UUID movieId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        List<Review> reviews = List.of(
                Review.builder().movieId(movieId).userId(UUID.randomUUID()).rating(5).title("Title1").content("Content1").createdOn(now).updatedOn(now).build(),
                Review.builder().movieId(movieId).userId(UUID.randomUUID()).rating(4).title("Title2").content("Content2").createdOn(now).updatedOn(now).build(),
                Review.builder().movieId(movieId).userId(UUID.randomUUID()).rating(3).title("Title3").content("Content3").createdOn(now).updatedOn(now).build()
        );
        when(reviewRepository.findAllByMovieIdOrderByUpdatedOnDesc(movieId)).thenReturn(reviews);

        List<ReviewResponse> result = reviewService.getLatestFiveReviews(movieId);

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void whenGetLatestFiveReviews_andNoReviewsExist_thenThrowException() {
        UUID movieId = UUID.randomUUID();
        when(reviewRepository.findAllByMovieIdOrderByUpdatedOnDesc(movieId)).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> reviewService.getLatestFiveReviews(movieId));
    }

    @Test
    void whenGetLatestFiveReviews_andMoreThanFiveReviewsExist_thenReturnOnlyFive() {
        UUID movieId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        List<Review> reviews = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            reviews.add(Review.builder()
                    .movieId(movieId)
                    .userId(UUID.randomUUID())
                    .rating(i % 5 + 1)
                    .title("Title" + i)
                    .content("Content" + i)
                    .createdOn(now)
                    .updatedOn(now)
                    .build());
        }
        when(reviewRepository.findAllByMovieIdOrderByUpdatedOnDesc(movieId)).thenReturn(reviews);

        List<ReviewResponse> result = reviewService.getLatestFiveReviews(movieId);

        assertNotNull(result);
        assertEquals(5, result.size());
    }

    @Test
    void whenGetReviewsForMovie_thenReturnPagedReviews() {
        UUID movieId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 5);
        List<Review> reviews = List.of(
                Review.builder().movieId(movieId).userId(UUID.randomUUID()).rating(5).title("Title1").content("Content1").createdOn(now).updatedOn(now).build(),
                Review.builder().movieId(movieId).userId(UUID.randomUUID()).rating(4).title("Title2").content("Content2").createdOn(now).updatedOn(now).build()
        );
        Page<Review> reviewPage = new PageImpl<>(reviews, pageable, reviews.size());
        when(reviewRepository.findByMovieIdOrderByUpdatedOnDesc(movieId, pageable)).thenReturn(reviewPage);

        Page<ReviewResponse> result = reviewService.getReviewsForMovie(movieId, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    void whenGetAllReviewsForAMovieCount_andReviewsExist_thenReturnCount() {
        UUID movieId = UUID.randomUUID();
        List<Review> reviews = List.of(
                Review.builder().movieId(movieId).rating(5).build(),
                Review.builder().movieId(movieId).rating(4).build()
        );
        when(reviewRepository.findAllByMovieId(movieId)).thenReturn(reviews);

        Integer result = reviewService.getAllReviewsForAMovieCount(movieId);

        assertEquals(2, result);
    }

    @Test
    void whenGetAllReviewsForAMovieCount_andNoReviewsExist_thenThrowNotFoundException() {
        UUID movieId = UUID.randomUUID();
        when(reviewRepository.findAllByMovieId(movieId)).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> reviewService.getAllReviewsForAMovieCount(movieId));
    }

    @Test
    void whenGetAllReviewedMoviesCountByUser_andReviewsExist_thenReturnCount() {
        UUID userId = UUID.randomUUID();
        List<Review> reviews = List.of(
                Review.builder().userId(userId).movieId(UUID.randomUUID()).rating(5).build(),
                Review.builder().userId(userId).movieId(UUID.randomUUID()).rating(4).build(),
                Review.builder().userId(userId).movieId(UUID.randomUUID()).rating(3).build()
        );
        when(reviewRepository.findAllByUserId(userId)).thenReturn(reviews);

        Integer result = reviewService.getAllReviewedMoviesCountByUser(userId);

        assertEquals(3, result);
    }

    @Test
    void whenGetAllReviewedMoviesCountByUser_andNoReviewsExist_thenThrowNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(reviewRepository.findAllByUserId(userId)).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> reviewService.getAllReviewedMoviesCountByUser(userId));
    }

    @Test
    void whenGetLatestReviewsByUserId_andReviewsExist_thenReturnLimitedReviewResponses() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        List<Review> reviews = List.of(
                Review.builder().userId(userId).movieId(UUID.randomUUID()).rating(5).title("Title1").content("Content1").createdOn(now).updatedOn(now).build(),
                Review.builder().userId(userId).movieId(UUID.randomUUID()).rating(4).title("Title2").content("Content2").createdOn(now).updatedOn(now).build()
        );
        when(reviewRepository.findAllByUserIdOrderByCreatedOnDesc(userId)).thenReturn(reviews);

        List<ReviewResponse> result = reviewService.getLatestReviewsByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void whenGetLatestReviewsByUserId_andNoReviewsExist_thenThrowNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(reviewRepository.findAllByUserIdOrderByCreatedOnDesc(userId)).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> reviewService.getLatestReviewsByUserId(userId));
    }

    @Test
    void whenGetLatestReviewsByUserId_andMoreThan20ReviewsExist_thenReturnOnly20() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        List<Review> reviews = new java.util.ArrayList<>();
        for (int i = 0; i < 25; i++) {
            reviews.add(Review.builder()
                    .userId(userId)
                    .movieId(UUID.randomUUID())
                    .rating(i % 5 + 1)
                    .title("Title" + i)
                    .content("Content" + i)
                    .createdOn(now)
                    .updatedOn(now)
                    .build());
        }
        when(reviewRepository.findAllByUserIdOrderByCreatedOnDesc(userId)).thenReturn(reviews);

        List<ReviewResponse> result = reviewService.getLatestReviewsByUserId(userId);

        assertNotNull(result);
        assertEquals(20, result.size());
    }
}

package app.popdreviewsvc.web;

import app.popdreviewsvc.exception.NotFoundException;
import app.popdreviewsvc.model.Review;
import app.popdreviewsvc.service.ReviewService;
import app.popdreviewsvc.web.dto.ReviewResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
public class ReviewControllerApiTest {

    @MockitoBean
    private ReviewService reviewService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postUpsertReview_shouldReturn201CreatedAndReturnReviewResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Review review = Review.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .movieId(movieId)
                .rating(5)
                .title("Great movie!")
                .content("This is an amazing film.")
                .createdOn(now)
                .updatedOn(now)
                .build();
        when(reviewService.upsert(any())).thenReturn(review);

        String requestBody = """
                {
                    "userId": "%s",
                    "movieId": "%s",
                    "rating": 5,
                    "title": "Great movie!",
                    "content": "This is an amazing film."
                }
                """.formatted(userId, movieId);

        MockHttpServletRequestBuilder httpRequest = post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        mockMvc.perform(httpRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.movieId").value(movieId.toString()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.title").value("Great movie!"))
                .andExpect(jsonPath("$.content").value("This is an amazing film."));

        verify(reviewService).upsert(any());
    }

    @Test
    void getReviewByUserAndMovie_shouldReturn200OkAndReturnReviewResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Review review = Review.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .movieId(movieId)
                .rating(4)
                .title("Good movie")
                .content("Enjoyed watching it.")
                .createdOn(now)
                .updatedOn(now)
                .build();
        when(reviewService.findByUserIdAndMovieId(userId, movieId)).thenReturn(review);

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/reviews/{userId}/{movieId}", userId, movieId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.movieId").value(movieId.toString()))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.title").value("Good movie"))
                .andExpect(jsonPath("$.content").value("Enjoyed watching it."));

        verify(reviewService).findByUserIdAndMovieId(userId, movieId);
    }

    @Test
    void getReviewByUserAndMovie_whenReviewNotFound_shouldReturn404NotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        when(reviewService.findByUserIdAndMovieId(userId, movieId))
                .thenThrow(new NotFoundException("Review not found"));

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/reviews/{userId}/{movieId}", userId, movieId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isNotFound());

        verify(reviewService).findByUserIdAndMovieId(userId, movieId);
    }

    @Test
    void deleteReview_shouldReturn204NoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        doNothing().when(reviewService).removeReview(userId, movieId);

        MockHttpServletRequestBuilder httpRequest = delete("/api/v1/reviews/{userId}/{movieId}", userId, movieId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isNoContent());

        verify(reviewService).removeReview(userId, movieId);
    }

    @Test
    void deleteReview_whenReviewNotFound_shouldReturn404NotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        doThrow(new NotFoundException("Review not found")).when(reviewService).removeReview(userId, movieId);

        MockHttpServletRequestBuilder httpRequest = delete("/api/v1/reviews/{userId}/{movieId}", userId, movieId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isNotFound());

        verify(reviewService).removeReview(userId, movieId);
    }

    @Test
    void getLatestReviewsForAMovie_shouldReturn200OkAndReturnListOfReviewResponses() throws Exception {
        UUID movieId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        List<ReviewResponse> responses = List.of(
                ReviewResponse.builder()
                        .userId(UUID.randomUUID())
                        .movieId(movieId)
                        .rating(5)
                        .title("Title1")
                        .content("Content1")
                        .createdOn(now)
                        .updatedOn(now)
                        .build(),
                ReviewResponse.builder()
                        .userId(UUID.randomUUID())
                        .movieId(movieId)
                        .rating(4)
                        .title("Title2")
                        .content("Content2")
                        .createdOn(now)
                        .updatedOn(now)
                        .build()
        );
        when(reviewService.getLatestReviews(movieId, 5)).thenReturn(responses);

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/reviews/{movieId}", movieId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[1].rating").value(4));

        verify(reviewService).getLatestReviews(movieId, 5);
    }

    @Test
    void getLatestReviewsForAMovie_whenNoReviewsFound_shouldReturn404NotFound() throws Exception {
        UUID movieId = UUID.randomUUID();
        when(reviewService.getLatestReviews(movieId, 5))
                .thenThrow(new NotFoundException("No reviews found"));

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/reviews/{movieId}", movieId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isNotFound());

        verify(reviewService).getLatestReviews(movieId, 5);
    }

    @Test
    void getReviewsForMovie_shouldReturn200OkAndReturnPageOfReviewResponses() throws Exception {
        UUID movieId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        List<ReviewResponse> responses = List.of(
                ReviewResponse.builder()
                        .userId(UUID.randomUUID())
                        .movieId(movieId)
                        .rating(5)
                        .title("Title1")
                        .content("Content1")
                        .createdOn(now)
                        .updatedOn(now)
                        .build(),
                ReviewResponse.builder()
                        .userId(UUID.randomUUID())
                        .movieId(movieId)
                        .rating(4)
                        .title("Title2")
                        .content("Content2")
                        .createdOn(now)
                        .updatedOn(now)
                        .build()
        );
        Page<ReviewResponse> page = new PageImpl<>(responses, PageRequest.of(0, 5), 2);
        when(reviewService.getReviewsForMovie(eq(movieId), any())).thenReturn(page);

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/reviews/{movieId}/page", movieId)
                .param("page", "0")
                .param("size", "5");

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].rating").value(5))
                .andExpect(jsonPath("$.content[1].rating").value(4))
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(reviewService).getReviewsForMovie(eq(movieId), any());
    }

    @Test
    void getMovieReviewStats_shouldReturn200OkAndReturnMovieReviewStatsResponse() throws Exception {
        UUID movieId = UUID.randomUUID();
        when(reviewService.getAllReviewsForAMovieCount(movieId)).thenReturn(10);

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/reviews/{movieId}/stats", movieId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReviews").value(10));

        verify(reviewService).getAllReviewsForAMovieCount(movieId);
    }

    @Test
    void getMovieReviewStats_whenNoReviewsFound_shouldReturn404NotFound() throws Exception {
        UUID movieId = UUID.randomUUID();
        when(reviewService.getAllReviewsForAMovieCount(movieId))
                .thenThrow(new NotFoundException("No reviews found"));

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/reviews/{movieId}/stats", movieId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isNotFound());

        verify(reviewService).getAllReviewsForAMovieCount(movieId);
    }

    @Test
    void getUserReviewStats_shouldReturn200OkAndReturnUserReviewsStatsResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        when(reviewService.getAllReviewedMoviesCountByUser(userId)).thenReturn(5);

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/reviews/{userId}/user", userId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewedMovies").value(5));

        verify(reviewService).getAllReviewedMoviesCountByUser(userId);
    }

    @Test
    void getUserReviewStats_whenNoReviewsFound_shouldReturn404NotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        when(reviewService.getAllReviewedMoviesCountByUser(userId))
                .thenThrow(new NotFoundException("No movies reviewed"));

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/reviews/{userId}/user", userId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isNotFound());

        verify(reviewService).getAllReviewedMoviesCountByUser(userId);
    }

    @Test
    void getLatestReviewsByUser_shouldReturn200OkAndReturnListOfReviewResponses() throws Exception {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        List<ReviewResponse> responses = List.of(
                ReviewResponse.builder()
                        .userId(userId)
                        .movieId(UUID.randomUUID())
                        .rating(5)
                        .title("Title1")
                        .content("Content1")
                        .createdOn(now)
                        .updatedOn(now)
                        .build(),
                ReviewResponse.builder()
                        .userId(userId)
                        .movieId(UUID.randomUUID())
                        .rating(4)
                        .title("Title2")
                        .content("Content2")
                        .createdOn(now)
                        .updatedOn(now)
                        .build()
        );
        when(reviewService.getLatestReviewsByUserId(userId)).thenReturn(responses);

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/reviews/{userId}/latest-reviews", userId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[1].rating").value(4));

        verify(reviewService).getLatestReviewsByUserId(userId);
    }

    @Test
    void getLatestReviewsByUser_whenNoReviewsFound_shouldReturn404NotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        when(reviewService.getLatestReviewsByUserId(userId))
                .thenThrow(new NotFoundException("No reviews found"));

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/reviews/{userId}/latest-reviews", userId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isNotFound());

        verify(reviewService).getLatestReviewsByUserId(userId);
    }
}

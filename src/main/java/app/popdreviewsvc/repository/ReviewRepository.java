package app.popdreviewsvc.repository;

import app.popdreviewsvc.model.Review;
import app.popdreviewsvc.web.dto.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findByUserIdAndMovieId(UUID userId, UUID movieId);

    List<ReviewResponse> findAllByMovieIdOrderByUpdatedOnDesc(UUID movieId);

    Page<Review> findByMovieIdOrderByUpdatedOnDesc(UUID movieId, Pageable pageable);
}

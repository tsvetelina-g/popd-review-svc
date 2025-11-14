package app.popdreviewsvc.repository;

import app.popdreviewsvc.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findByUserIdAndMovieId(UUID userId, UUID movieId);
}

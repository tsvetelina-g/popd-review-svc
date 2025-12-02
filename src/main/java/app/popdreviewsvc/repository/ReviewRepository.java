package app.popdreviewsvc.repository;

import app.popdreviewsvc.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends MongoRepository<Review, UUID> {

    Optional<Review> findByUserIdAndMovieId(UUID userId, UUID movieId);

    List<Review> findAllByMovieIdOrderByUpdatedOnDesc(UUID movieId);

    Page<Review> findByMovieIdOrderByUpdatedOnDesc(UUID movieId, Pageable pageable);

    List<Review> findAllByMovieId(UUID movieId);

    List<Review> findAllByUserId(UUID userId);

    List<Review> findAllByUserIdOrderByCreatedOnDesc(UUID userId);
}

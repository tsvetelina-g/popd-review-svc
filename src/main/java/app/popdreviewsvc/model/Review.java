package app.popdreviewsvc.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("reviews")
@CompoundIndex(def = "{'userId':1, 'movieId':1}", unique = true)
public class Review {

    @Id
    private UUID id;

    private Integer rating;

    private UUID userId;

    private UUID movieId;

    private String title;

    private String content;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
}

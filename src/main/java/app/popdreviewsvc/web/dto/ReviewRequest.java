package app.popdreviewsvc.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ReviewRequest {

    private UUID userId;

    private UUID movieId;

    private Integer rating;

    private String title;

    private String content;
}

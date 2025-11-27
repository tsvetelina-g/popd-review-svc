package app.popdreviewsvc.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserReviewsStatsResponse {

    private Integer reviewedMovies;
}

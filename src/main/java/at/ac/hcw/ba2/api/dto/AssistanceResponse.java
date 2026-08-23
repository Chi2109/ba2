package at.ac.hcw.ba2.api.dto;

import java.util.List;

public record AssistanceResponse(
        List<Recommendation> recommendations
) {
}

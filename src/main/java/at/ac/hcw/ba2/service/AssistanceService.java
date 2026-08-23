package at.ac.hcw.ba2.service;

import at.ac.hcw.ba2.api.dto.AssistanceRequest;
import at.ac.hcw.ba2.api.dto.AssistanceResponse;
import at.ac.hcw.ba2.api.dto.Recommendation;
import at.ac.hcw.ba2.domain.Priority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssistanceService {

    public AssistanceResponse generateAssistance(AssistanceRequest request) {
        Recommendation placeholder = new Recommendation(
                Priority.NORMAL,
                "Prototype request accepted. Retrieval and AI generation will be added in the next implementation steps.",
                "prototype-backend",
                null,
                false
        );

        return new AssistanceResponse(List.of(placeholder));
    }
}

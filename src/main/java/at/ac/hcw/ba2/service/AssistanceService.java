package at.ac.hcw.ba2.service;

import at.ac.hcw.ba2.api.dto.AssistanceRequest;
import at.ac.hcw.ba2.api.dto.AssistanceResponse;
import at.ac.hcw.ba2.api.dto.Recommendation;
import at.ac.hcw.ba2.domain.Priority;
import at.ac.hcw.ba2.knowledge.KnowledgeEntry;
import at.ac.hcw.ba2.knowledge.RetrievalService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssistanceService {

    private static final int MAX_RETRIEVED_ENTRIES = 3;

    private final RetrievalService retrievalService;

    public AssistanceService(
            RetrievalService retrievalService
    ) {
        this.retrievalService = retrievalService;
    }

    public AssistanceResponse generateAssistance(
            AssistanceRequest request
    ) {
        List<KnowledgeEntry> retrievedEntries =
                retrievalService.retrieve(
                        request,
                        MAX_RETRIEVED_ENTRIES
                );

        if (retrievedEntries.isEmpty()) {
            return new AssistanceResponse(
                    List.of(
                            new Recommendation(
                                    Priority.LOW,
                                    "No matching fictional knowledge-base entry was found. "
                                            + "The prototype cannot provide a grounded recommendation for this input.",
                                    "prototype-system",
                                    null,
                                    true
                            )
                    )
            );
        }

        List<Recommendation> recommendations =
                retrievedEntries.stream()
                        .map(this::toRecommendation)
                        .toList();

        return new AssistanceResponse(recommendations);
    }

    private Recommendation toRecommendation(
            KnowledgeEntry entry
    ) {
        return new Recommendation(
                entry.priority(),
                entry.text(),
                entry.id(),
                entry.requiredQualification(),
                false
        );
    }
}

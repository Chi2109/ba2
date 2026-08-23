package at.ac.hcw.ba2.service;

import at.ac.hcw.ba2.api.dto.AssistanceRequest;
import at.ac.hcw.ba2.api.dto.AssistanceResponse;
import at.ac.hcw.ba2.api.dto.Recommendation;
import at.ac.hcw.ba2.domain.Priority;
import at.ac.hcw.ba2.domain.Qualification;
import at.ac.hcw.ba2.knowledge.KnowledgeEntry;
import at.ac.hcw.ba2.knowledge.RetrievalService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssistanceService {

    private static final int MAX_RETRIEVED_ENTRIES = 3;

    private final RetrievalService retrievalService;
    private final QualificationService qualificationService;

    public AssistanceService(
            RetrievalService retrievalService,
            QualificationService qualificationService
    ) {
        this.retrievalService = retrievalService;
        this.qualificationService = qualificationService;
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
                        .map(entry ->
                                toRecommendation(entry, request))
                        .toList();

        return new AssistanceResponse(recommendations);
    }

    private Recommendation toRecommendation(
            KnowledgeEntry entry,
            AssistanceRequest request
    ) {
        Qualification requiredQualification =
                entry.requiredQualification();

        boolean qualified =
                qualificationService.isQualified(
                        request.teamQualification(),
                        requiredQualification
                );

        if (!qualified) {
            return new Recommendation(
                    entry.priority(),
                    buildEscalationText(requiredQualification),
                    entry.id(),
                    requiredQualification.jsonValue(),
                    true
            );
        }

        return new Recommendation(
                entry.priority(),
                entry.text(),
                entry.id(),
                requiredQualification == null
                        ? null
                        : requiredQualification.jsonValue(),
                false
        );
    }

    private String buildEscalationText(
            Qualification requiredQualification
    ) {
        return "The retrieved fictional source contains information "
                + "that requires qualification "
                + requiredQualification.jsonValue()
                + " in the prototype model. "
                + "The current team does not meet that requirement. "
                + "The restricted content is therefore not presented; "
                + "escalate to an appropriately qualified resource.";
    }
}

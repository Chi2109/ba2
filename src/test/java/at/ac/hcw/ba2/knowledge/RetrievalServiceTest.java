package at.ac.hcw.ba2.knowledge;

import at.ac.hcw.ba2.api.dto.AssistanceRequest;
import at.ac.hcw.ba2.domain.Qualification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RetrievalServiceTest {

    private RetrievalService retrievalService;

    @BeforeEach
    void setUp() throws IOException {
        KnowledgeBaseLoader loader =
                new KnowledgeBaseLoader(
                        new ObjectMapper()
                );

        loader.load();

        retrievalService =
                new RetrievalService(loader);
    }

    @Test
    void chestPainRetrievesAbcdeEscalationAndDocumentation() {
        AssistanceRequest request =
                new AssistanceRequest(
                        "chest pain",
                        "high",
                        Set.of(
                                Qualification.RS,
                                Qualification.NFS
                        ),
                        List.of(
                                "chest pressure",
                                "shortness of breath"
                        ),
                        "patient is pale and sweating"
                );

        List<KnowledgeEntry> results =
                retrievalService.retrieve(
                        request,
                        5
                );

        assertThat(results)
                .extracting(KnowledgeEntry::id)
                .contains(
                        "KB-ABCDE-001",
                        "KB-ESC-001",
                        "KB-DOC-001"
                );
    }

    @Test
    void retrievesQualificationBoundEntry() {
        AssistanceRequest request =
                new AssistanceRequest(
                        "qualification boundary",
                        "high",
                        Set.of(Qualification.RS),
                        List.of("advanced intervention"),
                        "fictional scenario"
                );

        List<KnowledgeEntry> results =
                retrievalService.retrieve(
                        request,
                        5
                );

        assertThat(results)
                .extracting(KnowledgeEntry::id)
                .contains(
                        "KB-QUAL-001",
                        "KB-ESC-001"
                );
    }

    @Test
    void unrelatedLowUrgencyInputReturnsNoKnowledge() {
        AssistanceRequest request =
                new AssistanceRequest(
                        "equipment issue",
                        "low",
                        Set.of(Qualification.RS),
                        List.of("broken tablet screen"),
                        "charging cable unavailable"
                );

        List<KnowledgeEntry> results =
                retrievalService.retrieve(
                        request,
                        5
                );

        assertThat(results).isEmpty();
    }

    @Test
    void respectsRequestedResultLimit() {
        AssistanceRequest request =
                new AssistanceRequest(
                        "chest pain",
                        "high",
                        Set.of(Qualification.RS),
                        List.of(
                                "chest pressure",
                                "shortness of breath"
                        ),
                        "pale and sweating"
                );

        List<KnowledgeEntry> results =
                retrievalService.retrieve(
                        request,
                        3
                );

        assertThat(results)
                .hasSizeLessThanOrEqualTo(3);
    }
}

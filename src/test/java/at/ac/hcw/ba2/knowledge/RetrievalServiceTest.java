package at.ac.hcw.ba2.knowledge;

import at.ac.hcw.ba2.api.dto.AssistanceRequest;
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
                new KnowledgeBaseLoader(new ObjectMapper());

        loader.load();

        retrievalService = new RetrievalService(loader);
    }

    @Test
    void retrievesChestPainKnowledge() {
        AssistanceRequest request =
                new AssistanceRequest(
                        "chest pain",
                        "high",
                        Set.of("RS", "NFS"),
                        List.of(
                                "chest pressure",
                                "shortness of breath"
                        ),
                        "patient is pale and sweating"
                );

        List<KnowledgeEntry> results =
                retrievalService.retrieve(request, 3);

        assertThat(results)
                .extracting(KnowledgeEntry::id)
                .contains("KB-CHEST-001");

        assertThat(results)
                .allMatch(entry -> entry.id().startsWith("KB-"));
    }

    @Test
    void respectsRequestedResultLimit() {
        AssistanceRequest request =
                new AssistanceRequest(
                        "chest pain",
                        "high",
                        Set.of("RS"),
                        List.of(
                                "chest pressure",
                                "shortness of breath"
                        ),
                        "pale and sweating"
                );

        List<KnowledgeEntry> results =
                retrievalService.retrieve(request, 2);

        assertThat(results).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    void returnsNoResultForUnrelatedInput() {
        AssistanceRequest request =
                new AssistanceRequest(
                        "equipment issue",
                        "low",
                        Set.of("RS"),
                        List.of("broken tablet screen"),
                        "charging cable unavailable"
                );

        List<KnowledgeEntry> results =
                retrievalService.retrieve(request, 3);

        assertThat(results).isEmpty();
    }
}

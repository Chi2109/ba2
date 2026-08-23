package at.ac.hcw.ba2.service;

import at.ac.hcw.ba2.api.dto.AssistanceRequest;
import at.ac.hcw.ba2.api.dto.AssistanceResponse;
import at.ac.hcw.ba2.knowledge.KnowledgeBaseLoader;
import at.ac.hcw.ba2.knowledge.RetrievalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AssistanceServiceTest {

    private AssistanceService assistanceService;

    @BeforeEach
    void setUp() throws IOException {
        KnowledgeBaseLoader loader =
                new KnowledgeBaseLoader(new ObjectMapper());

        loader.load();

        assistanceService =
                new AssistanceService(
                        new RetrievalService(loader)
                );
    }

    @Test
    void returnsSourceGroundedRecommendations() {
        AssistanceRequest request =
                new AssistanceRequest(
                        "chest pain",
                        "high",
                        Set.of("RS", "NFS"),
                        List.of(
                                "chest pressure",
                                "shortness of breath"
                        ),
                        "pale and sweating"
                );

        AssistanceResponse response =
                assistanceService.generateAssistance(request);

        assertThat(response.recommendations())
                .isNotEmpty()
                .allMatch(
                        recommendation ->
                                recommendation.source()
                                        .startsWith("KB-")
                );
    }

    @Test
    void returnsSafeFallbackWhenNothingMatches() {
        AssistanceRequest request =
                new AssistanceRequest(
                        "equipment issue",
                        "low",
                        Set.of("RS"),
                        List.of("broken tablet screen"),
                        "charging cable unavailable"
                );

        AssistanceResponse response =
                assistanceService.generateAssistance(request);

        assertThat(response.recommendations()).hasSize(1);

        assertThat(
                response.recommendations()
                        .getFirst()
                        .source()
        ).isEqualTo("prototype-system");

        assertThat(
                response.recommendations()
                        .getFirst()
                        .requiresEscalation()
        ).isTrue();
    }
}

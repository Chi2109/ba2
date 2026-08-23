package at.ac.hcw.ba2.api;

import at.ac.hcw.ba2.api.dto.AssistanceResponse;
import at.ac.hcw.ba2.api.dto.Recommendation;
import at.ac.hcw.ba2.domain.Priority;
import at.ac.hcw.ba2.service.AssistanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssistanceController.class)
class AssistanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssistanceService assistanceService;

    @Test
    void assistReturnsStructuredResponse() throws Exception {
        when(assistanceService.generateAssistance(any()))
                .thenReturn(
                        new AssistanceResponse(
                                List.of(
                                        new Recommendation(
                                                Priority.HIGH,
                                                "Test recommendation",
                                                "test-source",
                                                null,
                                                false
                                        )
                                )
                        )
                );

        mockMvc.perform(
                        post("/assist")
                                .contentType("application/json")
                                .content(validRequest())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.recommendations[0].priority"
                        ).value("high")
                )
                .andExpect(
                        jsonPath(
                                "$.recommendations[0].source"
                        ).value("test-source")
                );
    }

    @Test
    void previousEndpointRemainsAvailable() throws Exception {
        when(assistanceService.generateAssistance(any()))
                .thenReturn(
                        new AssistanceResponse(
                                List.of()
                        )
                );

        mockMvc.perform(
                        post("/api/v1/assistance")
                                .contentType("application/json")
                                .content(validRequest())
                )
                .andExpect(status().isOk());
    }

    @Test
    void rejectsMissingDispatchType() throws Exception {
        String requestBody = """
                {
                  "urgency": "high",
                  "teamQualification": ["RS"],
                  "symptoms": []
                }
                """;

        mockMvc.perform(
                        post("/assist")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnknownQualification() throws Exception {
        String requestBody = """
                {
                  "dispatchType": "test",
                  "urgency": "low",
                  "teamQualification": ["DOCTOR"],
                  "symptoms": ["test"]
                }
                """;

        mockMvc.perform(
                        post("/assist")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    private String validRequest() {
        return """
                {
                  "dispatchType": "chest pain",
                  "urgency": "high",
                  "teamQualification": ["RS", "NFS"],
                  "symptoms": [
                    "chest pressure",
                    "shortness of breath"
                  ],
                  "notes": "fictional test data"
                }
                """;
    }
}

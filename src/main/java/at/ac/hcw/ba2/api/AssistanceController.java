package at.ac.hcw.ba2.api;

import at.ac.hcw.ba2.api.dto.AssistanceRequest;
import at.ac.hcw.ba2.api.dto.AssistanceResponse;
import at.ac.hcw.ba2.service.AssistanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssistanceController {

    private final AssistanceService assistanceService;

    public AssistanceController(AssistanceService assistanceService) {
        this.assistanceService = assistanceService;
    }

    @PostMapping({"/assist", "/api/v1/assistance"})
    public ResponseEntity<AssistanceResponse> getAssistance(
            @Valid @RequestBody AssistanceRequest request
    ) {
        return ResponseEntity.ok(
                assistanceService.generateAssistance(request)
        );
    }
}

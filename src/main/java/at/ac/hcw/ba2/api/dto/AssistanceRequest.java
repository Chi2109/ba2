package at.ac.hcw.ba2.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public record AssistanceRequest(
        @NotBlank
        @Size(max = 100)
        String dispatchType,

        @NotBlank
        @Size(max = 30)
        String urgency,

        @NotEmpty
        Set<@NotBlank @Size(max = 20) String> teamQualification,

        @NotNull
        @Size(max = 20)
        List<@NotBlank @Size(max = 150) String> symptoms,

        @Size(max = 1000)
        String notes
) {
}

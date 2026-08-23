package at.ac.hcw.ba2.service;

import at.ac.hcw.ba2.domain.Qualification;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class QualificationServiceTest {

    private final QualificationService qualificationService =
            new QualificationService();

    @Test
    void rsSatisfiesRsRequirement() {
        assertThat(
                qualificationService.isQualified(
                        Set.of(Qualification.RS),
                        Qualification.RS
                )
        ).isTrue();
    }

    @Test
    void rsDoesNotSatisfyNfsRequirement() {
        assertThat(
                qualificationService.isQualified(
                        Set.of(Qualification.RS),
                        Qualification.NFS
                )
        ).isFalse();
    }

    @Test
    void nfsSatisfiesRsRequirement() {
        assertThat(
                qualificationService.isQualified(
                        Set.of(Qualification.NFS),
                        Qualification.RS
                )
        ).isTrue();
    }

    @Test
    void nfsSatisfiesNfsRequirement() {
        assertThat(
                qualificationService.isQualified(
                        Set.of(Qualification.NFS),
                        Qualification.NFS
                )
        ).isTrue();
    }

    @Test
    void noRequirementIsAlwaysSatisfied() {
        assertThat(
                qualificationService.isQualified(
                        Set.of(Qualification.RS),
                        null
                )
        ).isTrue();
    }
}

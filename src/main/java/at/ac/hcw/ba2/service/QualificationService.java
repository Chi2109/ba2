package at.ac.hcw.ba2.service;

import at.ac.hcw.ba2.domain.Qualification;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class QualificationService {

    public boolean isQualified(
            Set<Qualification> teamQualifications,
            Qualification requiredQualification
    ) {
        if (requiredQualification == null) {
            return true;
        }

        if (teamQualifications == null || teamQualifications.isEmpty()) {
            return false;
        }

        return teamQualifications.stream()
                .anyMatch(teamQualification ->
                        teamQualification.satisfies(requiredQualification));
    }
}

package at.ac.hcw.ba2.knowledge;

import at.ac.hcw.ba2.domain.Priority;
import at.ac.hcw.ba2.domain.Qualification;

import java.util.Set;

public record KnowledgeEntry(
        String id,
        String title,
        String text,
        Set<String> tags,
        Priority priority,
        Qualification requiredQualification
) {
}

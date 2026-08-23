package at.ac.hcw.ba2.knowledge;

import at.ac.hcw.ba2.api.dto.AssistanceRequest;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class RetrievalService {

    private final KnowledgeBaseLoader knowledgeBaseLoader;

    public RetrievalService(KnowledgeBaseLoader knowledgeBaseLoader) {
        this.knowledgeBaseLoader = knowledgeBaseLoader;
    }

    public List<KnowledgeEntry> retrieve(
            AssistanceRequest request,
            int limit
    ) {
        if (limit <= 0) {
            return List.of();
        }

        String context = buildSearchContext(request);

        return knowledgeBaseLoader.getEntries()
                .stream()
                .map(entry -> new ScoredEntry(entry, score(entry, context)))
                .filter(scoredEntry -> scoredEntry.score() > 0)
                .sorted(
                        Comparator.comparingInt(ScoredEntry::score)
                                .reversed()
                                .thenComparingInt(
                                        scoredEntry ->
                                                scoredEntry.entry()
                                                        .priority()
                                                        .ordinal()
                                )
                                .thenComparing(
                                        scoredEntry ->
                                                scoredEntry.entry().id()
                                )
                )
                .limit(limit)
                .map(ScoredEntry::entry)
                .toList();
    }

    private int score(
            KnowledgeEntry entry,
            String normalizedContext
    ) {
        int score = 0;

        for (String tag : entry.tags()) {
            String normalizedTag = normalize(tag);

            if (!normalizedTag.isBlank()
                    && normalizedContext.contains(normalizedTag)) {
                score += 10;
            }
        }

        String normalizedTitle = normalize(entry.title());

        for (String titleToken : tokenize(normalizedTitle)) {
            if (titleToken.length() >= 4
                    && normalizedContext.contains(titleToken)) {
                score += 2;
            }
        }

        return score;
    }

    private String buildSearchContext(
            AssistanceRequest request
    ) {
        List<String> parts = new ArrayList<>();

        parts.add(request.dispatchType());
        parts.add(request.urgency());
        parts.addAll(request.symptoms());

        if (request.notes() != null) {
            parts.add(request.notes());
        }

        return normalize(String.join(" ", parts));
    }

    private List<String> tokenize(String value) {
        if (value.isBlank()) {
            return List.of();
        }

        return List.of(value.split("\\s+"));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        String decomposed = Normalizer.normalize(
                value,
                Normalizer.Form.NFD
        );

        return decomposed
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record ScoredEntry(
            KnowledgeEntry entry,
            int score
    ) {
    }
}

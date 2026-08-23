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

    private static final String ABCDE_ID = "KB-ABCDE-001";
    private static final String ESCALATION_ID = "KB-ESC-001";
    private static final String DOCUMENTATION_ID = "KB-DOC-001";

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

        List<KnowledgeEntry> ranked =
                knowledgeBaseLoader.getEntries()
                        .stream()
                        .filter(entry ->
                                !entry.id().equals(DOCUMENTATION_ID))
                        .map(entry ->
                                new ScoredEntry(
                                        entry,
                                        score(entry, request, context)
                                ))
                        .filter(scoredEntry ->
                                scoredEntry.score() > 0)
                        .sorted(
                                Comparator.comparingInt(
                                                ScoredEntry::score
                                        )
                                        .reversed()
                                        .thenComparingInt(
                                                scoredEntry ->
                                                        scoredEntry.entry()
                                                                .priority()
                                                                .ordinal()
                                        )
                                        .thenComparing(
                                                scoredEntry ->
                                                        scoredEntry.entry()
                                                                .id()
                                        )
                        )
                        .limit(limit)
                        .map(ScoredEntry::entry)
                        .toList();

        if (ranked.isEmpty()) {
            return List.of();
        }

        List<KnowledgeEntry> result =
                new ArrayList<>(ranked);

        if (result.size() < limit) {
            findById(DOCUMENTATION_ID)
                    .ifPresent(result::add);
        }

        return List.copyOf(result);
    }

    private int score(
            KnowledgeEntry entry,
            AssistanceRequest request,
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

        for (String titleToken :
                tokenize(normalize(entry.title()))) {
            if (titleToken.length() >= 4
                    && normalizedContext.contains(titleToken)) {
                score += 2;
            }
        }

        if (entry.id().equals(ABCDE_ID)
                && shouldRetrieveAbcde(
                        request,
                        normalizedContext
                )) {
            score += 30;
        }

        if (entry.id().equals(ESCALATION_ID)
                && isHighUrgency(request)) {
            score += 30;
        }

        return score;
    }

    private boolean shouldRetrieveAbcde(
            AssistanceRequest request,
            String normalizedContext
    ) {
        return isHighUrgency(request)
                || containsAny(
                        normalizedContext,
                        "chest pain",
                        "chest pressure",
                        "shortness of breath",
                        "breathing",
                        "circulation",
                        "airway"
                );
    }

    private boolean isHighUrgency(
            AssistanceRequest request
    ) {
        return "high".equals(
                normalize(request.urgency())
        );
    }

    private boolean containsAny(
            String value,
            String... terms
    ) {
        for (String term : terms) {
            if (value.contains(normalize(term))) {
                return true;
            }
        }

        return false;
    }

    private java.util.Optional<KnowledgeEntry> findById(
            String id
    ) {
        return knowledgeBaseLoader.getEntries()
                .stream()
                .filter(entry ->
                        entry.id().equals(id))
                .findFirst();
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

        String decomposed =
                Normalizer.normalize(
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

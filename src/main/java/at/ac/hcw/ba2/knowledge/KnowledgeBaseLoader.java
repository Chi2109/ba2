package at.ac.hcw.ba2.knowledge;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class KnowledgeBaseLoader {

    private static final String KNOWLEDGE_BASE_PATH =
            "knowledge/knowledge-base.json";

    private final ObjectMapper objectMapper;

    private List<KnowledgeEntry> entries = List.of();

    public KnowledgeBaseLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void load() throws IOException {
        ClassPathResource resource =
                new ClassPathResource(KNOWLEDGE_BASE_PATH);

        try (InputStream inputStream = resource.getInputStream()) {
            List<KnowledgeEntry> loadedEntries =
                    objectMapper.readValue(
                            inputStream,
                            new TypeReference<List<KnowledgeEntry>>() {
                            }
                    );

            if (loadedEntries.isEmpty()) {
                throw new IllegalStateException(
                        "Knowledge base must contain at least one entry."
                );
            }

            entries = List.copyOf(loadedEntries);
        }
    }

    public List<KnowledgeEntry> getEntries() {
        return entries;
    }
}

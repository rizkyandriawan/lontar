package lontar.service;

import lontar.model.Tag;
import lontar.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    public Optional<Tag> findById(UUID id) {
        return tagRepository.findById(id);
    }

    public Optional<Tag> findBySlug(String slug) {
        return tagRepository.findBySlug(slug);
    }

    public Optional<Tag> findByName(String name) {
        return tagRepository.findByName(name);
    }

    public Tag create(String name, String description) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setSlug(generateSlug(name));
        tag.setDescription(description);
        return tagRepository.save(tag);
    }

    public Tag findOrCreate(String name) {
        return tagRepository.findByName(name.trim()).orElseGet(() -> create(name.trim(), null));
    }

    public Tag update(Tag tag) {
        return tagRepository.save(tag);
    }

    public void delete(UUID id) {
        tagRepository.deleteById(id);
    }

    private String generateSlug(String name) {
        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        String slug = base;
        int counter = 2;
        while (tagRepository.findBySlug(slug).isPresent()) {
            slug = base + "-" + counter;
            counter++;
        }
        return slug;
    }
}

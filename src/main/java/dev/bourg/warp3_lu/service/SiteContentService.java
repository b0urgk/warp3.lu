package dev.bourg.warp3_lu.service;

import dev.bourg.warp3_lu.model.SiteContent;
import dev.bourg.warp3_lu.repository.SiteContentRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SiteContentService {

    private final SiteContentRepository repository;

    public SiteContentService(SiteContentRepository repository) {
        this.repository = repository;
    }

    public String get(String key) {
        return repository.findById(key)
                .map(SiteContent::getContentValue)
                .orElse("");
    }

    public String get(String key, String defaultValue) {
        return repository.findById(key)
                .map(SiteContent::getContentValue)
                .orElse(defaultValue);
    }

    public void set(String key, String value) {
        repository.save(new SiteContent(key, value));
    }

    public Map<String, String> getAll() {
        return new HashMap<>(repository.findAll().stream()
                .collect(Collectors.toMap(SiteContent::getContentKey, SiteContent::getContentValue)));
    }
}

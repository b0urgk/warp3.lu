package dev.bourg.warp3_lu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.bourg.warp3_lu.dto.CalendarEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExternalEventService {

    private static final Logger log = LoggerFactory.getLogger(ExternalEventService.class);
    private static final long CACHE_TTL_MS = 15 * 60 * 1000; // 15 minutes

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String externalUrl;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public ExternalEventService(@Value("${events.external.url:}") String externalUrl) {
        this.externalUrl = externalUrl;
        this.restClient = RestClient.create();
    }

    public List<CalendarEvent> fetchAll() {
        if (externalUrl == null || externalUrl.isBlank()) {
            return Collections.emptyList();
        }

        CacheEntry cached = cache.get("all");
        if (cached != null && !cached.isExpired()) {
            return cached.events;
        }

        try {
            // Use URI.create() to prevent RestClient from re-encoding the URL
            String json = restClient.get()
                    .uri(URI.create(externalUrl))
                    .retrieve()
                    .body(String.class);

            List<CalendarEvent> events = parseEvents(objectMapper.readTree(json));
            log.info("Fetched {} external events from wiki", events.size());
            cache.put("all", new CacheEntry(events));
            return events;
        } catch (Exception e) {
            log.warn("Failed to fetch external events: {}", e.getMessage());
            if (cached != null) {
                return cached.events; // return stale cache on error
            }
            return Collections.emptyList();
        }
    }

    public List<CalendarEvent> findByMonth(int year, int month) {
        List<CalendarEvent> all = fetchAll();
        return all.stream()
                .filter(e -> e.getStartTime().getYear() == year
                        && e.getStartTime().getMonthValue() == month)
                .toList();
    }

    private List<CalendarEvent> parseEvents(JsonNode root) {
        List<CalendarEvent> events = new ArrayList<>();
        try {
            JsonNode results = root.get("results");
            if (results == null || !results.isObject()) {
                return events;
            }

            results.fields().forEachRemaining(entry -> {
                try {
                    JsonNode printouts = entry.getValue().get("printouts");
                    if (printouts == null) return;

                    LocalDateTime startTime = parseTimestamp(printouts.get("StartDate"));
                    LocalDateTime endTime = parseTimestamp(printouts.get("EndDate"));
                    if (startTime == null || endTime == null) return;

                    String title = entry.getKey();
                    String subtitle = getFirstText(printouts.get("Has subtitle"));
                    String description = getFirstText(printouts.get("Has description"));
                    String eventType = getFirstText(printouts.get("Is Event of Type"));

                    String location = null;
                    JsonNode locNode = printouts.get("Has location");
                    if (locNode != null && locNode.isArray() && !locNode.isEmpty()) {
                        location = locNode.get(0).path("fulltext").asText(null);
                    }

                    String wikiUrl = entry.getValue().path("fullurl").asText(null);
                    if (wikiUrl != null && wikiUrl.startsWith("//")) {
                        wikiUrl = "https:" + wikiUrl;
                    }

                    events.add(CalendarEvent.fromExternal(
                            title, subtitle, description, location,
                            startTime, endTime, eventType, wikiUrl));
                } catch (Exception e) {
                    log.debug("Skipping external event '{}': {}", entry.getKey(), e.getMessage());
                }
            });
        } catch (Exception e) {
            log.warn("Failed to parse external events JSON: {}", e.getMessage());
        }
        return events;
    }

    private LocalDateTime parseTimestamp(JsonNode node) {
        if (node == null || !node.isArray() || node.isEmpty()) return null;
        try {
            long epochSeconds = Long.parseLong(node.get(0).asText());
            return LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(epochSeconds),
                    ZoneId.of("Europe/Luxembourg"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getFirstText(JsonNode node) {
        if (node == null || !node.isArray() || node.isEmpty()) return null;
        String text = node.get(0).asText(null);
        return (text != null && !text.isBlank()) ? text : null;
    }

    private static class CacheEntry {
        final List<CalendarEvent> events;
        final long timestamp;

        CacheEntry(List<CalendarEvent> events) {
            this.events = events;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }
}

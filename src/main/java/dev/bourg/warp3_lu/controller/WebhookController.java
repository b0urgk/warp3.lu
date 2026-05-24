package dev.bourg.warp3_lu.controller;

import dev.bourg.warp3_lu.service.SiteContentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

@RestController
@RequestMapping("/webhook/space")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);
    private static final String STATUS_KEY = "home.status";

    private final SiteContentService siteContentService;
    private final String secret;

    public WebhookController(SiteContentService siteContentService,
                             @Value("${webhook.space.secret:}") String secret) {
        this.siteContentService = siteContentService;
        this.secret = secret;
    }

    @PostMapping("/open")
    public ResponseEntity<String> open(@RequestHeader(value = "X-Webhook-Token", required = false) String token) throws Exception {
        if (!authorized(token)) {
            return ResponseEntity.status(401).body("unauthorized");
        }
        setSpaceStatus("open");
        return ResponseEntity.ok("open");
    }

    @PostMapping("/close")
    public ResponseEntity<String> close(@RequestHeader(value = "X-Webhook-Token", required = false) String token) throws Exception{
        if (!authorized(token)) {
            return ResponseEntity.status(401).body("unauthorized");
        }

        setSpaceStatus("closed");
        return ResponseEntity.ok("closed");
    }

    private boolean authorized(String token) {
        if (secret == null || secret.isBlank()) {
            log.warn("Webhook called but webhook.space.secret is not configured");
            return false;
        }
        return token != null && constantTimeEquals(secret, token);
    }

    private boolean setSpaceStatus(String state) throws Exception{
        if (!Set.of("open", "closed").contains(state)) return false;

        siteContentService.set(STATUS_KEY, state);
        long unixTimestamp = Instant.now().getEpochSecond();

        String apiKey = System.getenv("SPACE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            log.error("SPACE_API_KEY is not configured");
            return false;
        }
        String sensorsJson = String.format(
                "{\"state\":{\"open\":%s,\"lastchange\":%d}}",
                state.equals("open"),
                unixTimestamp
        );

        String formData =
                "key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8) +
                        "&sensors=" + URLEncoder.encode(sensorsJson, StandardCharsets.UTF_8);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://spaceapi.syn2cat.lu/sensor/set"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
        log.info("Space status set to {} via webhook", state);
        return true;
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}

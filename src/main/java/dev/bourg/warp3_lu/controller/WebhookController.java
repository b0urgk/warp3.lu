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
    public ResponseEntity<String> open(@RequestHeader(value = "X-Webhook-Token", required = false) String token) {
        if (!authorized(token)) {
            return ResponseEntity.status(401).body("unauthorized");
        }
        siteContentService.set(STATUS_KEY, "open");
        log.info("Space status set to open via webhook");
        return ResponseEntity.ok("open");
    }

    @PostMapping("/close")
    public ResponseEntity<String> close(@RequestHeader(value = "X-Webhook-Token", required = false) String token) {
        if (!authorized(token)) {
            return ResponseEntity.status(401).body("unauthorized");
        }
        siteContentService.set(STATUS_KEY, "closed");
        log.info("Space status set to closed via webhook");
        return ResponseEntity.ok("closed");
    }

    private boolean authorized(String token) {
        if (secret == null || secret.isBlank()) {
            log.warn("Webhook called but webhook.space.secret is not configured");
            return false;
        }
        return token != null && constantTimeEquals(secret, token);
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

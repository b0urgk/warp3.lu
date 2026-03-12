package dev.bourg.warp3_lu.model;

import jakarta.persistence.*;

@Entity
@Table(name = "site_content")
public class SiteContent {

    @Id
    @Column(name = "content_key", nullable = false, unique = true)
    private String contentKey;

    @Column(name = "content_value", columnDefinition = "TEXT")
    private String contentValue;

    public SiteContent() {}

    public SiteContent(String contentKey, String contentValue) {
        this.contentKey = contentKey;
        this.contentValue = contentValue;
    }

    public String getContentKey() { return contentKey; }
    public void setContentKey(String contentKey) { this.contentKey = contentKey; }

    public String getContentValue() { return contentValue; }
    public void setContentValue(String contentValue) { this.contentValue = contentValue; }
}

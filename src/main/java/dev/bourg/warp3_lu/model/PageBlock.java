package dev.bourg.warp3_lu.model;

import jakarta.persistence.*;

@Entity
@Table(name = "page_blocks")
public class PageBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlockType blockType;

    @Column(nullable = false)
    private int position;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String contentHtml;

    @Column(columnDefinition = "TEXT")
    private String contentSecondary;

    @Column(columnDefinition = "TEXT")
    private String contentSecondaryHtml;

    private String imageUrl;

    private String altText;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Page getPage() { return page; }
    public void setPage(Page page) { this.page = page; }

    public BlockType getBlockType() { return blockType; }
    public void setBlockType(BlockType blockType) { this.blockType = blockType; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getContentHtml() { return contentHtml; }
    public void setContentHtml(String contentHtml) { this.contentHtml = contentHtml; }

    public String getContentSecondary() { return contentSecondary; }
    public void setContentSecondary(String contentSecondary) { this.contentSecondary = contentSecondary; }

    public String getContentSecondaryHtml() { return contentSecondaryHtml; }
    public void setContentSecondaryHtml(String contentSecondaryHtml) { this.contentSecondaryHtml = contentSecondaryHtml; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }
}

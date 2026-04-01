package dev.bourg.warp3_lu.service;

import dev.bourg.warp3_lu.model.BlockType;
import dev.bourg.warp3_lu.model.Page;
import dev.bourg.warp3_lu.model.PageBlock;
import dev.bourg.warp3_lu.repository.PageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PageService {

    private final PageRepository pageRepository;
    private final MarkdownService markdownService;

    public PageService(PageRepository pageRepository, MarkdownService markdownService) {
        this.pageRepository = pageRepository;
        this.markdownService = markdownService;
    }

    public List<Page> findAll() {
        return pageRepository.findAll();
    }

    public List<Page> findPublished() {
        return pageRepository.findByStatusOrderByCreatedAtDesc(Page.Status.PUBLISHED);
    }

    public Optional<Page> findById(Long id) {
        return pageRepository.findById(id);
    }

    public Optional<Page> findBySlug(String slug) {
        return pageRepository.findBySlug(slug);
    }

    @Transactional
    public Page save(Page page) {
        for (int i = 0; i < page.getBlocks().size(); i++) {
            PageBlock block = page.getBlocks().get(i);
            block.setPage(page);
            block.setPosition(i);

            if (block.getBlockType() == BlockType.MARKDOWN) {
                block.setContentHtml(renderMarkdown(block.getContent()));
            } else if (block.getBlockType() == BlockType.COLUMNS) {
                block.setContentHtml(renderMarkdown(block.getContent()));
                block.setContentSecondaryHtml(renderMarkdown(block.getContentSecondary()));
            }
        }

        if (page.getStatus() == Page.Status.PUBLISHED && page.getPublishedAt() == null) {
            page.setPublishedAt(LocalDateTime.now());
        }

        return pageRepository.save(page);
    }

    private String renderMarkdown(String markdown) {
        if (markdown == null || markdown.isBlank()) return "";
        return markdownService.toHtml(markdown);
    }

    public void delete(Long id) {
        pageRepository.deleteById(id);
    }
}

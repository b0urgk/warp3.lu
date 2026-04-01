package dev.bourg.warp3_lu.repository;

import dev.bourg.warp3_lu.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PageRepository extends JpaRepository<Page, Long> {

    Optional<Page> findBySlug(String slug);

    List<Page> findByStatusOrderByCreatedAtDesc(Page.Status status);
}

package dev.bourg.warp3_lu.service;

import dev.bourg.warp3_lu.model.Post;
import dev.bourg.warp3_lu.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final MarkdownService markdownService;

    public PostService(PostRepository postRepository, MarkdownService markdownService){
        this.postRepository = postRepository;
        this.markdownService = markdownService;
    }

    public List<Post> findAll(){
        return postRepository.findAll();
    }

    public List<Post> findPublished(){
        return postRepository.findByStatusOrderByPublishedAtDesc(Post.Status.PUBLISHED);
    }

    public Optional<Post> findById(Long id){
        return postRepository.findById(id);
    }

    public Optional<Post> findBySlug(String slug){
        return postRepository.findBySlug(slug);
    }

    public Post save(Post post) {
        post.setContentHtml(markdownService.toHtml(post.getContent()));
        if (post.getStatus() == Post.Status.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
        return postRepository.save(post);
    }

    public void delete(Long id){
        postRepository.deleteById(id);
    }
}
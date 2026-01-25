package dev.bourg.warp3_lu.controller;
import dev.bourg.warp3_lu.model.Post;
import dev.bourg.warp3_lu.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PublicController {

    private final PostService postService;

    public PublicController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("posts", postService.findPublished());
        model.addAttribute("pageTitle", "Home");
        model.addAttribute("pageName", "home");

        return "public/home";
    }

    @GetMapping("/blog")
    public String blog(Model model) {
        model.addAttribute("posts", postService.findPublished());
        return "public/blog";
    }

    @GetMapping("/blog/{slug}")
    public String post(@PathVariable String slug, Model model) {
        Post post = postService.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Only show published posts
        if (post.getStatus() != Post.Status.PUBLISHED) {
            throw new RuntimeException("Post not found");
        }

        model.addAttribute("post", post);
        return "public/post";
    }
}
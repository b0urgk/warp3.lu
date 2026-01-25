package dev.bourg.warp3_lu.controller;

import dev.bourg.warp3_lu.model.Post;
import dev.bourg.warp3_lu.model.User;
import dev.bourg.warp3_lu.repository.UserRepository;
import dev.bourg.warp3_lu.service.PostService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final PostService postService;
    private final UserRepository userRepository;


    public AdminController(PostService postService, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.postService = postService;
    }

    @GetMapping
    public String dashboard(Model model){
        model.addAttribute("posts", postService.findAll());
        return "admin/dashboard";
    }

    @GetMapping("/posts/new")
    public String newPost(Model model){
        model.addAttribute("post", new Post());
        return "admin/post-form";
    }

    @GetMapping("/posts/edit/{id}")
    public String editPost(@PathVariable Long id, Model model){
        Post post = postService.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        model.addAttribute("post", post);
        return "admin/post-form";
    }

    @PostMapping("/posts/save")
    public String savePost(@ModelAttribute Post post,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        if (post.getId() == null) {
            User author = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            post.setAuthor(author);
        }

        postService.save(post);
        redirectAttributes.addFlashAttribute("message", "Post saved successfully");
        return "redirect:/admin";
    }

    @PostMapping("/posts/publish/{id}")
    public String publishPost(@PathVariable Long id, RedirectAttributes redirectAttributes){
        Post post = postService.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.publish();
        postService.save(post);
        redirectAttributes.addFlashAttribute("message", "Post published");
        return "redirect:/admin";
    }

    @PostMapping("/posts/delete/{id}")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes){
        postService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Post deleted!");
        return "redirect:/admin";
    }

}

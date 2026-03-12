package dev.bourg.warp3_lu.controller;

import dev.bourg.warp3_lu.model.Event;
import dev.bourg.warp3_lu.model.Post;
import dev.bourg.warp3_lu.model.User;
import dev.bourg.warp3_lu.repository.UserRepository;
import dev.bourg.warp3_lu.service.EventService;
import dev.bourg.warp3_lu.service.PostService;
import dev.bourg.warp3_lu.service.SiteContentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PostService postService;
    private final EventService eventService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SiteContentService siteContentService;

    public AdminController(PostService postService, EventService eventService,
                           UserRepository userRepository, PasswordEncoder passwordEncoder,
                           SiteContentService siteContentService) {
        this.postService = postService;
        this.eventService = eventService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.siteContentService = siteContentService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("posts", postService.findAll());
        model.addAttribute("events", eventService.findUpcoming());
        model.addAttribute("users", userRepository.findAll());
        Map<String, String> siteContent = siteContentService.getAll();
        siteContent.putIfAbsent("home.status", "closed");
        siteContent.putIfAbsent("home.what", "Community-operated space for tinkering, building, and sharing knowledge about technology.");
        siteContent.putIfAbsent("home.where", "35 rue du Chemin de Fer\nDifferdange, Luxembourg");
        siteContent.putIfAbsent("home.when", "Tuesdays 20:00 onwards\n+ whenever the door's open");
        siteContent.putIfAbsent("home.links", "Wiki|https://wiki.syn2cat.lu\nGitHub|https://github.com/syn2cat\nContact|mailto:info@syn2cat.lu");
        model.addAttribute("siteContent", siteContent);
        model.addAttribute("pageName", "dashboard");
        model.addAttribute("pageTitle", "Dashboard");
        return "admin/dashboard";
    }

    // ===================== POSTS =====================

    @GetMapping("/posts/new")
    public String newPost(Model model) {
        model.addAttribute("post", new Post());
        model.addAttribute("pageName", "post-form");
        model.addAttribute("customCss", List.of("post", "post-form"));
        model.addAttribute("pageTitle", "New post");

        return "admin/post-form";
    }

    @GetMapping("/posts/edit/{id}")
    public String editPost(@PathVariable Long id, Model model) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        model.addAttribute("post", post);
        model.addAttribute("customCss", List.of("post", "post-form"));
        model.addAttribute("pageTitle", "Edit post");
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
    public String publishPost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.publish();
        postService.save(post);
        redirectAttributes.addFlashAttribute("message", "Post published");
        return "redirect:/admin";
    }

    @PostMapping("/posts/delete/{id}")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        postService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Post deleted!");
        return "redirect:/admin";
    }

    // ===================== EVENTS =====================

    @GetMapping("/events/new")
    public String newEvent(Model model) {
        model.addAttribute("event", new Event());
        model.addAttribute("posts", postService.findPublished());
        model.addAttribute("recurrenceTypes", Event.RecurrenceType.values());
        model.addAttribute("pageTitle", "New event");
        return "admin/event-form";
    }

    @GetMapping("/events/edit/{id}")
    public String editEvent(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        model.addAttribute("event", event);
        model.addAttribute("posts", postService.findPublished());
        model.addAttribute("recurrenceTypes", Event.RecurrenceType.values());
        model.addAttribute("pageTitle", "Edit event");
        return "admin/event-form";
    }

    @PostMapping("/events/save")
    public String saveEvent(@ModelAttribute Event event,
                            @RequestParam(required = false) Long linkedPostId,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        if (event.getId() == null) {
            User organizer = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            event.setOrganizer(organizer);
        }

        if (linkedPostId != null) {
            Post linkedPost = postService.findById(linkedPostId)
                    .orElse(null);
            event.setLinkedPost(linkedPost);
        }

        eventService.save(event);
        redirectAttributes.addFlashAttribute("message", "Event saved successfully");
        return "redirect:/admin";
    }

    @PostMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        eventService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Event deleted!");
        return "redirect:/admin";
    }

    // ===================== USERS =====================

    @GetMapping("/users/new")
    public String newUser(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("pageName", "user-form");
        model.addAttribute("pageTitle", "New user");
        return "admin/user-form";
    }

    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("pageName", "user-form");
        model.addAttribute("pageTitle", "Edit user");
        return "admin/user-form";
    }

    @PostMapping("/users/save")
    public String saveUser(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam(required = false) String password,
                           @RequestParam User.Role role,
                           @RequestParam(required = false) Long id,
                           RedirectAttributes redirectAttributes) {
        User user;
        if (id != null) {
            user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setUsername(username);
            user.setEmail(email);
            user.setRole(role);
            // Only update password if a new one was provided
            if (password != null && !password.isBlank()) {
                user.setPassword(passwordEncoder.encode(password));
            }
        } else {
            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setRole(role);
            user.setPassword(passwordEncoder.encode(password));
        }
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("message", "User saved successfully");
        return "redirect:/admin";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getUsername().equals(userDetails.getUsername())) {
            redirectAttributes.addFlashAttribute("message", "Cannot delete your own account");
            return "redirect:/admin";
        }
        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "User deleted!");
        return "redirect:/admin";
    }

    @PostMapping("/content/save")
    public String saveContent(@RequestParam Map<String, String> params,
                              RedirectAttributes redirectAttributes) {
        params.forEach((key, value) -> {
            if (key.startsWith("content.")) {
                siteContentService.set(key.substring("content.".length()), value);
            }
        });
        redirectAttributes.addFlashAttribute("message", "Content saved successfully");
        return "redirect:/admin";
    }
}
package dev.bourg.warp3_lu.controller;
import dev.bourg.warp3_lu.model.Event;
import dev.bourg.warp3_lu.model.Post;
import dev.bourg.warp3_lu.service.EventService;
import dev.bourg.warp3_lu.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Controller
public class PublicController {

    private final PostService postService;
    private final EventService eventService;

    public PublicController(PostService postService, EventService eventService) {
        this.postService = postService;
        this.eventService = eventService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("posts", postService.findPublished());
        model.addAttribute("pageTitle", "Home");
        model.addAttribute("pageName", "home");

        return "public/home";
    }

    @GetMapping({"/blog", "/blog/"})
    public String blog(Model model) {
        model.addAttribute("posts", postService.findPublished());
        model.addAttribute("pageTitle", "Blog");
        model.addAttribute("pageName", "blog");
        return "public/blog";
    }

    @GetMapping({"/events", "/events/"})
    public String events(@RequestParam(required = false) Integer year,
                         @RequestParam(required = false) Integer month,
                         Model model) {
        LocalDate today = LocalDate.now();
        if (year == null || month == null) {
            year = today.getYear();
            month = today.getMonthValue();
        }

        YearMonth ym = YearMonth.of(year, month);
        YearMonth prev = ym.minusMonths(1);
        YearMonth next = ym.plusMonths(1);

        List<Event> monthEvents = eventService.findByMonth(year, month);
        Map<Integer, List<Event>> eventsByDay = new HashMap<>();
        for (Event e : monthEvents) {
            int day = e.getStartTime().getDayOfMonth();
            eventsByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(e);
        }

        LocalDate firstOfMonth = ym.atDay(1);
        int startOffset = firstOfMonth.getDayOfWeek().getValue() - 1;
        int daysInMonth = ym.lengthOfMonth();
        YearMonth prevMonth = ym.minusMonths(1);
        int prevMonthDays = prevMonth.lengthOfMonth();

        List<List<Map<String, Object>>> weeks = new ArrayList<>();
        List<Map<String, Object>> currentWeek = new ArrayList<>();

        for (int i = startOffset - 1; i >= 0; i--) {
            int day = prevMonthDays - i;
            currentWeek.add(Map.of(
                    "dayOfMonth", day,
                    "isToday", false,
                    "isCurrentMonth", false,
                    "events", List.of()
            ));
        }

        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date = ym.atDay(d);
            List<Event> dayEvents = eventsByDay.getOrDefault(d, List.of());
            currentWeek.add(Map.of(
                    "dayOfMonth", d,
                    "isToday", date.equals(today),
                    "isCurrentMonth", true,
                    "events", dayEvents
            ));
            if (currentWeek.size() == 7) {
                weeks.add(currentWeek);
                currentWeek = new ArrayList<>();
            }
        }

        if (!currentWeek.isEmpty()) {
            int nextDay = 1;
            while (currentWeek.size() < 7) {
                currentWeek.add(Map.of(
                        "dayOfMonth", nextDay++,
                        "isToday", false,
                        "isCurrentMonth", false,
                        "events", List.of()
                ));
            }
            weeks.add(currentWeek);
        }

        String monthLabel = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;

        model.addAttribute("weeks", weeks);
        model.addAttribute("monthEvents", monthEvents);
        model.addAttribute("monthLabel", monthLabel);
        model.addAttribute("prevYear", prev.getYear());
        model.addAttribute("prevMonth", prev.getMonthValue());
        model.addAttribute("nextYear", next.getYear());
        model.addAttribute("nextMonth", next.getMonthValue());
        model.addAttribute("pageTitle", "Events");
        model.addAttribute("pageName", "events");
        return "public/events";
    }

    @GetMapping("/blog/{slug}")
    public String post(@PathVariable String slug, Model model) {
        Post post = postService.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getStatus() != Post.Status.PUBLISHED) {
            throw new RuntimeException("Post not found");
        }

        model.addAttribute("post", post);
        model.addAttribute("pageTitle", post.getTitle());
        model.addAttribute("pageName", "post");
        return "public/post";
    }
}
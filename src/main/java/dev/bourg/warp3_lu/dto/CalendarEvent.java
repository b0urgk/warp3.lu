package dev.bourg.warp3_lu.dto;

import java.time.LocalDateTime;

public class CalendarEvent {

    public enum Source { LOCAL, EXTERNAL }

    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean allDay;
    private Source source;
    private String eventType;
    private String wikiUrl;
    private String subtitle;
    private String linkedPostSlug;

    public static CalendarEvent fromLocal(
            String title, String description, String location,
            LocalDateTime startTime, LocalDateTime endTime, boolean allDay,
            String linkedPostSlug) {
        CalendarEvent e = new CalendarEvent();
        e.title = title;
        e.description = description;
        e.location = location;
        e.startTime = startTime;
        e.endTime = endTime;
        e.allDay = allDay;
        e.source = Source.LOCAL;
        e.linkedPostSlug = linkedPostSlug;
        return e;
    }

    public static CalendarEvent fromExternal(
            String title, String subtitle, String description,
            String location, LocalDateTime startTime, LocalDateTime endTime,
            String eventType, String wikiUrl) {
        CalendarEvent e = new CalendarEvent();
        e.title = title;
        e.subtitle = subtitle;
        e.description = description;
        e.location = location;
        e.startTime = startTime;
        e.endTime = endTime;
        e.allDay = false;
        e.source = Source.EXTERNAL;
        e.eventType = eventType;
        e.wikiUrl = wikiUrl;
        return e;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public boolean isAllDay() { return allDay; }
    public Source getSource() { return source; }
    public String getEventType() { return eventType; }
    public String getWikiUrl() { return wikiUrl; }
    public String getSubtitle() { return subtitle; }
    public String getLinkedPostSlug() { return linkedPostSlug; }
    public boolean isExternal() { return source == Source.EXTERNAL; }
}

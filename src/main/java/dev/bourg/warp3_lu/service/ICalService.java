package dev.bourg.warp3_lu.service;

import dev.bourg.warp3_lu.dto.CalendarEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ICalService {

    private static final DateTimeFormatter ICAL_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final String PRODID = "-//warp3.lu//Events//EN";

    public String generateFeed(List<CalendarEvent> events) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:").append(PRODID).append("\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");
        sb.append("X-WR-CALNAME:warp3.lu Events\r\n");
        sb.append("X-WR-TIMEZONE:Europe/Luxembourg\r\n");

        for (CalendarEvent event : events) {
            appendEvent(sb, event);
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    public String generateSingle(CalendarEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:").append(PRODID).append("\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");

        appendEvent(sb, event);

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private void appendEvent(StringBuilder sb, CalendarEvent event) {
        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append(generateUid(event)).append("\r\n");
        sb.append("DTSTART:").append(formatDateTime(event.getStartTime())).append("\r\n");
        sb.append("DTEND:").append(formatDateTime(event.getEndTime())).append("\r\n");
        sb.append("SUMMARY:").append(escapeIcal(event.getTitle())).append("\r\n");

        if (event.getDescription() != null) {
            sb.append("DESCRIPTION:").append(escapeIcal(event.getDescription())).append("\r\n");
        }
        if (event.getLocation() != null) {
            sb.append("LOCATION:").append(escapeIcal(event.getLocation())).append("\r\n");
        }
        if (event.isExternal() && event.getWikiUrl() != null) {
            sb.append("URL:").append(event.getWikiUrl()).append("\r\n");
        }

        sb.append("END:VEVENT\r\n");
    }

    private String formatDateTime(LocalDateTime dt) {
        return dt.format(ICAL_FORMAT);
    }

    private String escapeIcal(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace(",", "\\,")
                .replace(";", "\\;")
                .replace("\n", "\\n");
    }

    private String generateUid(CalendarEvent event) {
        String seed = event.getTitle() + event.getStartTime().toString();
        return UUID.nameUUIDFromBytes(seed.getBytes()) + "@warp3.lu";
    }
}

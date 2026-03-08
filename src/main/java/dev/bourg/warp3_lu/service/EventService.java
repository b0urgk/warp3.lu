package dev.bourg.warp3_lu.service;

import dev.bourg.warp3_lu.model.Event;
import dev.bourg.warp3_lu.repository.EventRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    public List<Event> findUpcoming() {
        return eventRepository.findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now());
    }

    public List<Event> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByStartTimeBetweenOrderByStartTimeAsc(start, end);
    }

    public List<Event> findByMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);
        return findByDateRange(start, end);
    }

    public List<Event> findByPost(Long postId) {
        return eventRepository.findByLinkedPostId(postId);
    }

    public Event save(Event event) {
        return eventRepository.save(event);
    }

    public void delete(Long id) {
        eventRepository.deleteById(id);
    }

    public List<Event> generateRecurringInstances(Event parentEvent, LocalDateTime until) {
        if (!parentEvent.isRecurring()) {
            return List.of(parentEvent);
        }

        List<Event> instances = new java.util.ArrayList<>();
        LocalDateTime currentStart = parentEvent.getStartTime();
        LocalDateTime currentEnd = parentEvent.getEndTime();
        long duration = java.time.Duration.between(currentStart, currentEnd).toMinutes();

        LocalDateTime endDate = parentEvent.getRecurrenceEndDate() != null
                ? parentEvent.getRecurrenceEndDate()
                : until;

        while (!currentStart.isAfter(endDate)) {
            Event instance = new Event();
            instance.setTitle(parentEvent.getTitle());
            instance.setDescription(parentEvent.getDescription());
            instance.setLocation(parentEvent.getLocation());
            instance.setStartTime(currentStart);
            instance.setEndTime(currentStart.plusMinutes(duration));
            instance.setAllDay(parentEvent.isAllDay());
            instance.setOrganizer(parentEvent.getOrganizer());
            instance.setLinkedPost(parentEvent.getLinkedPost());
            instance.setParentEvent(parentEvent);
            instances.add(instance);

            currentStart = advanceByRecurrence(currentStart, parentEvent);
            if (currentStart == null) break;
        }

        return instances;
    }

    private LocalDateTime advanceByRecurrence(LocalDateTime date, Event event) {
        int interval = event.getRecurrenceInterval() != null ? event.getRecurrenceInterval() : 1;

        return switch (event.getRecurrenceType()) {
            case DAILY -> date.plusDays(interval);
            case WEEKLY -> date.plusWeeks(interval);
            case MONTHLY -> date.plusMonths(interval);
            case YEARLY -> date.plusYears(interval);
            case NONE -> null;
        };
    }
}
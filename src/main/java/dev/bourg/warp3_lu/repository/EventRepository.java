package dev.bourg.warp3_lu.repository;

import dev.bourg.warp3_lu.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStartTimeBetweenOrderByStartTimeAsc(LocalDateTime start, LocalDateTime end);

    List<Event> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime date);

    List<Event> findByOrganizerId(Long organizerId);

    List<Event> findByLinkedPostId(Long postId);

    List<Event> findByParentEventId(Long parentEventId);

    List<Event> findByRecurrenceTypeNot(Event.RecurrenceType type);
}
package au.edu.rmit.sept.webapp.services;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.Event.EventStatus;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // Create a new event
    public Event createEvent(Event event) {
        validateEvent(event);

        // Set default values
        if (event.getCurrentRsvpCount() == null) {
            event.setCurrentRsvpCount(0);
        }

        if (event.getStatus() == null) {
            event.setStatus(EventStatus.draft);
        }

        if (event.getIsPaid() == null) {
            event.setIsPaid(false);
        }

        if (event.getIsFeatured() == null) {
            event.setIsFeatured(false);
        }

        Event savedEvent = eventRepository.save(event);

        // Generate QR code if event is published
        if (savedEvent.getStatus() == EventStatus.published) {
            generateQrCodeData(savedEvent);
        }

        return savedEvent;
    }

    // Update an existing event
    public Event updateEvent(Long eventId, Event updatedEvent) {
        Event existingEvent = getEventById(eventId);

        // Update fields
        existingEvent.setTitle(updatedEvent.getTitle());
        existingEvent.setDescription(updatedEvent.getDescription());
        existingEvent.setEventDate(updatedEvent.getEventDate());
        existingEvent.setStartTime(updatedEvent.getStartTime());
        existingEvent.setEndTime(updatedEvent.getEndTime());
        existingEvent.setLocation(updatedEvent.getLocation());
        existingEvent.setVenueDetails(updatedEvent.getVenueDetails());
        existingEvent.setMaxCapacity(updatedEvent.getMaxCapacity());
        existingEvent.setCategoryId(updatedEvent.getCategoryId());
        existingEvent.setClubId(updatedEvent.getClubId());
        existingEvent.setRegistrationDeadline(updatedEvent.getRegistrationDeadline());
        existingEvent.setIsPaid(updatedEvent.getIsPaid());
        existingEvent.setPrice(updatedEvent.getPrice());
        existingEvent.setCurrency(updatedEvent.getCurrency());
        existingEvent.setStatus(updatedEvent.getStatus());
        existingEvent.setIsFeatured(updatedEvent.getIsFeatured());
        existingEvent.setGoogleMapsUrl(updatedEvent.getGoogleMapsUrl());

        // Note: Cover image handling will be added later
        if (updatedEvent.getCoverImage() != null) {
            existingEvent.setCoverImage(updatedEvent.getCoverImage());
        }

        validateEvent(existingEvent);

        Event savedEvent = eventRepository.save(existingEvent);

        // Generate QR code if status changed to published and QR code doesn't exist
        if (savedEvent.getStatus() == EventStatus.published &&
                (savedEvent.getQrCodeData() == null || savedEvent.getQrCodeData().isEmpty())) {
            generateQrCodeData(savedEvent);
        }

        return savedEvent;
    }

    // Get event by ID
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
    }

    // Get all events by organiser
    public List<Event> getEventsByOrganiser(Long organiserId) {
        return eventRepository.findByCreatedByOrderByEventDateDesc(organiserId);
    }

    public Page<Event> getEventsByOrganiser(Long organiserId, Pageable pageable) {
        return eventRepository.findByCreatedByOrderByEventDateDesc(organiserId, pageable);
    }

    // Get published events
    public List<Event> getPublishedEvents() {
        return eventRepository.findByStatusOrderByEventDateAsc(EventStatus.published);
    }

    public Page<Event> getPublishedEvents(Pageable pageable) {
        return eventRepository.findByStatusOrderByEventDateAsc(EventStatus.published, pageable);
    }

    // Get upcoming events
    public List<Event> getUpcomingEvents() {
        return eventRepository.findUpcomingEvents(EventStatus.published, LocalDate.now());
    }

    public Page<Event> getUpcomingEvents(Pageable pageable) {
        return eventRepository.findUpcomingEvents(EventStatus.published, LocalDate.now(), pageable);
    }

    // Get featured events
    public List<Event> getFeaturedEvents() {
        return eventRepository.findByIsFeaturedTrueAndStatusOrderByEventDateAsc(EventStatus.published);
    }

    // Get all events (admin only - includes all statuses)
    public List<Event> getAllEvents() {
        return eventRepository.findAll().stream()
                .sorted((e1, e2) -> e1.getEventDate().compareTo(e2.getEventDate()))
                .collect(java.util.stream.Collectors.toList());
    }

    // Search events
    public List<Event> searchEvents(String searchTerm) {
        return eventRepository.searchEvents(EventStatus.published, searchTerm);
    }

    public Page<Event> searchEvents(String searchTerm, Pageable pageable) {
        return eventRepository.searchEvents(EventStatus.published, searchTerm, pageable);
    }

    // Get events by category
    public List<Event> getEventsByCategory(Long categoryId) {
        return eventRepository.findByCategoryIdAndStatusOrderByEventDateAsc(categoryId, EventStatus.published);
    }

    // Get events by club
    public List<Event> getEventsByClub(Long clubId) {
        return eventRepository.findByClubIdAndStatusOrderByEventDateAsc(clubId, EventStatus.published);
    }

    // Get events by date range
    public List<Event> getEventsByDateRange(LocalDate startDate, LocalDate endDate) {
        return eventRepository.findEventsByDateRange(EventStatus.published, startDate, endDate);
    }

    // Get events with available spots
    public List<Event> getEventsWithAvailableSpots() {
        return eventRepository.findEventsWithAvailableSpots(EventStatus.published);
    }

    // Get free events
    public List<Event> getFreeEvents() {
        return eventRepository.findFreeEvents(EventStatus.published);
    }

    // Get paid events
    public List<Event> getPaidEvents() {
        return eventRepository.findPaidEvents(EventStatus.published);
    }

    // Delete event (only if no RSVPs and organizer owns it) - for organizers only
    public void deleteEvent(Long eventId, Long organiserId) {
        Event event = getEventById(eventId);

        // Check if user is the organiser (admin check not applicable here since this method is for organizers)
        if (!event.getCreatedBy().equals(organiserId)) {
            throw new RuntimeException("You can only delete your own events");
        }

        // Check if event has RSVPs
        if (event.getCurrentRsvpCount() > 0) {
            throw new RuntimeException("Cannot delete event with existing RSVPs. Cancel the event instead.");
        }

        // Note: Image deletion will be added when image upload is implemented

        eventRepository.delete(event);
    }

    // Delete event for admin (bypasses organizer restriction and RSVP check)
    @Transactional
    public void deleteEventByAdmin(Long eventId) {
        Event event = getEventById(eventId);

        // Admin can delete any event regardless of RSVPs
        // Note: Image deletion will be added when image upload is implemented

        eventRepository.delete(event);
    }

    // Cancel event - for organizers
    public Event cancelEvent(Long eventId, Long organiserId) {
        Event event = getEventById(eventId);

        // Check if user is the organiser
        if (!event.getCreatedBy().equals(organiserId)) {
            throw new RuntimeException("You can only cancel your own events");
        }

        // Check if event is not already cancelled or completed
        if (event.getStatus() == EventStatus.cancelled) {
            throw new RuntimeException("Event is already cancelled");
        }

        if (event.getStatus() == EventStatus.completed) {
            throw new RuntimeException("Cannot cancel a completed event");
        }

        event.setStatus(EventStatus.cancelled);
        return eventRepository.save(event);
    }

    // Cancel event for admin (bypasses organizer restriction)
    @Transactional
    public Event cancelEventByAdmin(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Check if event is not already cancelled or completed
        if (event.getStatus() == EventStatus.cancelled) {
            throw new RuntimeException("Event is already cancelled");
        }

        if (event.getStatus() == EventStatus.completed) {
            throw new RuntimeException("Cannot cancel a completed event");
        }

        // Cancel the event
        event.setStatus(EventStatus.cancelled);
        
        try {
            return eventRepository.save(event);
        } catch (Exception e) {
            // Log the actual error for debugging
            System.err.println("Error cancelling event " + eventId + ": " + e.getMessage());
            throw new RuntimeException("Failed to cancel event. Original error: " + e.getMessage());
        }
    }


    // Uncancel event for admin (restore cancelled event to active status)
    @Transactional
    public Event uncancelEventByAdmin(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Check if event is actually cancelled
        if (event.getStatus() != EventStatus.cancelled) {
            throw new RuntimeException("Only cancelled events can be uncancelled");
        }

        // Restore to published status
        event.setStatus(EventStatus.published);
        
        try {
            Event savedEvent = eventRepository.save(event);
            
            // Generate QR code data if it doesn't exist
            generateQrCodeData(savedEvent);
            
            return savedEvent;
        } catch (Exception e) {
            System.err.println("Error uncancelling event " + eventId + ": " + e.getMessage());
            throw new RuntimeException("Failed to uncancel event. Original error: " + e.getMessage());
        }
    }

    // Publish event
    public Event publishEvent(Long eventId, Long organiserId) {
        Event event = getEventById(eventId);

        // Check if user is the organiser
        if (!event.getCreatedBy().equals(organiserId)) {
            throw new RuntimeException("You can only publish your own events");
        }

        event.setStatus(EventStatus.published);
        Event savedEvent = eventRepository.save(event);

        // Generate QR code
        generateQrCodeData(savedEvent);

        return savedEvent;
    }

    // Generate QR code data
    private void generateQrCodeData(Event event) {
        // Generate QR code data containing event URL
        String eventUrl = baseUrl + "/events/" + event.getEventId();
        event.setQrCodeData(eventUrl);
        eventRepository.save(event);
    }

    // Validate event
    private void validateEvent(Event event) {
        // Check if end time is after start time
        if (event.getStartTime() != null && event.getEndTime() != null) {
            if (!event.getEndTime().isAfter(event.getStartTime())) {
                throw new RuntimeException("End time must be after start time");
            }
        }

        // Check if event date is in the future (allow today)
        if (event.getEventDate() != null && event.getEventDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Event date must be today or in the future");
        }

        // Check if registration deadline is before event date
        if (event.getRegistrationDeadline() != null && event.getEventDate() != null) {
            LocalDate deadlineDate = event.getRegistrationDeadline().toLocalDate();
            if (deadlineDate.isAfter(event.getEventDate())) {
                throw new RuntimeException("Registration deadline must be before or on event date");
            }
        }

        // Check paid event validation
        if (event.getIsPaid() != null && event.getIsPaid()) {
            if (event.getPrice() == null || event.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Price is required for paid events");
            }
            if (event.getCurrency() == null || event.getCurrency().trim().isEmpty()) {
                throw new RuntimeException("Currency is required for paid events");
            }
        }

        // Check if duplicate event exists (same title, date, and organiser)
        if (event.getEventId() == null) { // Only check for new events
            boolean exists = eventRepository.existsByTitleAndEventDateAndCreatedBy(
                    event.getTitle(), event.getEventDate(), event.getCreatedBy());
            if (exists) {
                throw new RuntimeException("An event with the same title already exists on this date");
            }
        }
    }

    // Increment RSVP count
    public void incrementRsvpCount(Long eventId) {
        Event event = getEventById(eventId);
        if (event.getCurrentRsvpCount() >= event.getMaxCapacity()) {
            throw new RuntimeException("Event is at maximum capacity");
        }
        event.setCurrentRsvpCount(event.getCurrentRsvpCount() + 1);
        eventRepository.save(event);
    }

    // Decrement RSVP count
    public void decrementRsvpCount(Long eventId) {
        Event event = getEventById(eventId);
        if (event.getCurrentRsvpCount() > 0) {
            event.setCurrentRsvpCount(event.getCurrentRsvpCount() - 1);
            eventRepository.save(event);
        }
    }

    // Get event statistics for organiser dashboard
    public EventStatistics getEventStatistics(Long organiserId) {
        Object[] stats = eventRepository.getEventStatistics(organiserId);
        if (stats.length > 0 && stats[0] != null) {
            Object[] row = (Object[]) stats[0];
            return new EventStatistics(
                    ((Number) row[0]).longValue(), // published
                    ((Number) row[1]).longValue(), // draft
                    ((Number) row[2]).longValue(), // upcoming
                    ((Number) row[3]).longValue() // past
            );
        }
        return new EventStatistics(0L, 0L, 0L, 0L);
    }

    // Get today's events
    public List<Event> getTodaysEvents() {
        return eventRepository.findByEventDateAndStatusOrderByStartTimeAsc(
                LocalDate.now(), EventStatus.published);
    }

    // Get events with upcoming registration deadlines
    public List<Event> getEventsWithUpcomingDeadlines(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(days);
        return eventRepository.findEventsWithUpcomingDeadline(EventStatus.published, now, deadline);
    }

    // Mark event as completed (for past events)
    public void markEventAsCompleted(Long eventId) {
        Event event = getEventById(eventId);
        if (event.getEventDate().isBefore(LocalDate.now())) {
            event.setStatus(EventStatus.completed);
            eventRepository.save(event);
        }
    }

    // Batch update past events to completed status
    public void updatePastEventsToCompleted() {
        List<Event> pastEvents = eventRepository.findEventsByDateRange(
                EventStatus.published, LocalDate.of(2000, 1, 1), LocalDate.now().minusDays(1));

        for (Event event : pastEvents) {
            event.setStatus(EventStatus.completed);
        }
        eventRepository.saveAll(pastEvents);
    }

    // Check if user can edit event
    public boolean canEditEvent(Long eventId, Long userId) {
        Event event = getEventById(eventId);
        return event.getCreatedBy().equals(userId) &&
                event.getStatus() != EventStatus.completed &&
                event.getStatus() != EventStatus.cancelled;
    }

    // Get events count by status for organiser
    public long getEventCountByStatus(Long organiserId, EventStatus status) {
        return eventRepository.countByCreatedByAndStatus(organiserId, status);
    }

    // Inner class for event statistics
    public static class EventStatistics {
        private final long publishedCount;
        private final long draftCount;
        private final long upcomingCount;
        private final long pastCount;

        public EventStatistics(long publishedCount, long draftCount, long upcomingCount, long pastCount) {
            this.publishedCount = publishedCount;
            this.draftCount = draftCount;
            this.upcomingCount = upcomingCount;
            this.pastCount = pastCount;
        }

        public long getPublishedCount() {
            return publishedCount;
        }

        public long getDraftCount() {
            return draftCount;
        }

        public long getUpcomingCount() {
            return upcomingCount;
        }

        public long getPastCount() {
            return pastCount;
        }

        public long getTotalCount() {
            return publishedCount + draftCount;
        }
    }
}
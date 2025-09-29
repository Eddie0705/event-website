package au.edu.rmit.sept.webapp.services;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Unit tests for EventService
class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /** createEvent */
    @Test
    void createEventSuccess() {
        Event event = new Event();
        event.setTitle("Test Event");

        when(eventRepository.save(event)).thenReturn(event);

        Event saved = eventService.createEvent(event);

        assertEquals("Test Event", saved.getTitle());
    }

    /** getEventById */
    @Test
    void getEventByIdFound() {
        Event event = new Event();
        event.setCategoryId(1L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        Event result = eventService.getEventById(1L);;
        assertEquals(1L, result.getCategoryId());
    }

    @Test
    void getEventByIdNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> eventService.getEventById(1L));
        assertEquals("Event not found with id: 1", ex.getMessage());
    }

    /** deleteEvent */
@Test
void deleteEventSuccess() {
    Event event = new Event();
    event.setCategoryId(1L);
    User organiser = new User();
    organiser.setUserId(1L);
    event.setCreatedBy(1L);

    when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

    eventService.deleteEvent(1L, 1L);
    verify(eventRepository, times(1)).delete(event);

}

@Test
void deleteEventNotFound() {
    when(eventRepository.findById(1L)).thenReturn(Optional.empty());

    RuntimeException ex = assertThrows(RuntimeException.class, () -> eventService.deleteEvent(1L, 1L));
    assertEquals("Event not found with id: 1", ex.getMessage());
}

}

package au.edu.rmit.sept.webapp.services;

import au.edu.rmit.sept.webapp.models.*;
import au.edu.rmit.sept.webapp.models.RSVP.RSVPStatus;
import au.edu.rmit.sept.webapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RSVPService {
    private final RSVPRepository rsvpRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public RSVPService(RSVPRepository rsvpRepository,
                       EventRepository eventRepository,
                       UserRepository userRepository) {
        this.rsvpRepository = rsvpRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RSVP rsvpToEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<RSVP> existing = rsvpRepository.findByEventAndUser(event, user);
        if (existing.isPresent()) {
            RSVP rsvp = existing.get();
            rsvp.setRSVPStatus(RSVPStatus.YES);
            return rsvpRepository.save(rsvp);
        }

        RSVP rsvp = new RSVP();
        rsvp.setEvent(event);
        rsvp.setUser(user);
        rsvp.setRSVPStatus(RSVPStatus.YES);
        return rsvpRepository.save(rsvp);
    }

    @Transactional
    public void cancelRSVP(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        RSVP rsvp = rsvpRepository.findByEventAndUser(event, user)
                .orElseThrow(() -> new IllegalArgumentException("RSVP not found"));

        rsvp.setRSVPStatus(RSVPStatus.CANCELLED);
        rsvpRepository.save(rsvp);
    }

    public boolean hasRSVP(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return rsvpRepository.existsByEventAndUser(event, user);
    }
}

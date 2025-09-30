package au.edu.rmit.sept.webapp.services;

import au.edu.rmit.sept.webapp.models.*;
import au.edu.rmit.sept.webapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
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
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        return createOrUpdateRsvp(user, event, RSVP.RSVPStatus.yes, null);
    }

    @Transactional
    public void cancelRSVP(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        RSVP rsvp = rsvpRepository.findByEventAndUser(event, user)
                .orElseGet(() -> {
                    RSVP newRsvp = new RSVP();
                    newRsvp.setEvent(event);
                    newRsvp.setUser(user);
                    return newRsvp;
                });

        rsvp.setRSVPStatus(RSVP.RSVPStatus.cancelled);
        rsvpRepository.save(rsvp);
    }

    public boolean hasRSVP(Long eventId, Long userId) {
        Optional<Event> event = eventRepository.findById(eventId);
        Optional<User> user = userRepository.findById(userId);

        return event.isPresent() && user.isPresent() &&
               rsvpRepository.existsByEventAndUser(event.get(), user.get());
    }

    @Transactional
    public RSVP createOrUpdateRsvp(User user, Event event,
                                   RSVP.RSVPStatus rsvpStatus,
                                   RSVP.AttendanceStatus attendanceStatus) {

        RSVP rsvp = rsvpRepository.findByEventAndUser(event, user)
                .orElseGet(() -> {
                    RSVP newRsvp = new RSVP();
                    newRsvp.setUser(user);
                    newRsvp.setEvent(event);
                    newRsvp.setAttendanceStatus(RSVP.AttendanceStatus.not_checked);
                    return newRsvp;
                });

        rsvp.setRSVPStatus(rsvpStatus);
        if (attendanceStatus != null) {
            rsvp.setAttendanceStatus(attendanceStatus);
        }

        return rsvpRepository.save(rsvp);
    }


    @Transactional
    public String checkUserRsvp(Event event, User user) {
        return rsvpRepository.getRsvpStatus(event, user)
                .map(RSVP.RSVPStatus::name)
                .orElse("No RSVP yet");
    }

    @Transactional
    public String checkdsdddUserRsvp(Long eventId, Long userId) {
        Optional<Event> event = eventRepository.findById(eventId);
        Optional<User> user = userRepository.findById(userId);

        if (event.isEmpty() || user.isEmpty()) return "no";

        return rsvpRepository.getRsvpStatus(event.get(), user.get())
                .map(RSVP.RSVPStatus::name)
                .orElse("No RSVP yet");
        }
        
    @Transactional
    public Optional <RSVP> getUserRsvp (Event event, User user){
        return rsvpRepository.findByEventAndUser(event, user);
    }

    @Transactional
    public List<RSVP> getUserRsvps(User user) {
        return rsvpRepository.findByUserUserId(user.getUserId());
    }
}

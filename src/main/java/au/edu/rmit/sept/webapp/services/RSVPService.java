package au.edu.rmit.sept.webapp.services;

import au.edu.rmit.sept.webapp.models.*;
import au.edu.rmit.sept.webapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
@Service
public class RSVPService {
    private final RSVPRepository rsvpRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public RSVPService(RSVPRepository rsvpRepository,
                       EventRepository eventRepository,
                       UserRepository userRepository,
                       EmailService emailService) {
        this.rsvpRepository = rsvpRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public RSVP rsvpToEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if event has finished
        if (event.isEventFinished()) {
            throw new IllegalStateException("Cannot RSVP to an event that has already finished.");
        }

        RSVP rsvp = createOrUpdateRsvp(user, event, RSVP.RSVPStatus.yes, null);
        
        // send confirmation email (yes message)
        sendRSVPConfirmationEmail(user, event, "yes");

        return rsvp;
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
        // send confirmation email (cancelled message)
        sendRSVPConfirmationEmail(user, event, "cancelled");
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

        // Check if event has finished (only for RSVP, not for cancellation)
        if (rsvpStatus == RSVP.RSVPStatus.yes && event.isEventFinished()) {
            throw new IllegalStateException("Cannot RSVP to an event that has already finished.");
        }

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

        RSVP rsvpSaved =  rsvpRepository.save(rsvp);


        // send confirmation email (enum as message param)
        sendRSVPConfirmationEmail(user, event, rsvp.getRsvpStatus().toString());


        return rsvpSaved;
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



     // check attendance status
    @Transactional
    public String checkAttendanceStatus(Long eventId, Long userId) {

        return rsvpRepository.getAttendanceStatus(eventId, userId)
                .map(RSVP.AttendanceStatus::name)
                .orElse("Not Checked");
    }

    // update attendance status (check-in time also)
    @Transactional
    public boolean updateAttendanceStatus(Long eventId, Long userId, RSVP.AttendanceStatus status) {

        // validate inputs
        if (eventId == null || userId == null || status == null) {
            throw new IllegalArgumentException("Event ID, User ID, and Status must not be null");
        }

        // check if event exist
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        
        // check date (cannot check-in before event date)
        LocalDate today = LocalDate.now();
        if (event.getEventDate().isAfter(today) || event.getEventDate().isBefore(today)) {
            throw new IllegalArgumentException("Cannot check-in before the event date");
        }



        int updatedRows = rsvpRepository.updateAttendanceStatus(eventId, userId, status);
        return updatedRows > 0;
    }

    // helper method to send emails out 
    private void sendRSVPConfirmationEmail(User user, Event event, String action) {
        String recipient = user.getEmail();

        // create subject and message based on action
        // string cases match to RSVPStatus enum names
        String subject = switch (action) {
            case "yes" -> "RSVP Confirmation for " + event.getTitle();
            case "cancelled" -> "RSVP Cancellation for " + event.getTitle();
            default -> "RSVP Update for " + event.getTitle();
        };

        String message = switch (action) {
            case "yes" -> String.format(
                    "Hello %s,\n\nThank you for RSVPing to '%s'.\n\n" +
                    "Event Details:\n• Date: %s\n• Time: %s - %s\n• Location: %s\n\n" +
                    "We look forward to seeing you there!\n\n- The Events Team",
                    user.getFullName(),
                    event.getTitle(),
                    event.getEventDate(),
                    event.getStartTime(),
                    event.getEndTime(),
                    event.getLocation());
            case "cancelled" -> String.format(
                    "Hello %s,\n\nYour RSVP for '%s' has been successfully cancelled.\n\n" +
                    "We're sorry you can't make it, but hope to see you next time!\n\n- The Events Team",
                    user.getFullName(),
                    event.getTitle());
            default -> String.format(
                    "Hello %s,\n\nYour RSVP status for '%s' has been updated.\n\n- The Events Team",
                    user.getFullName(),
                    event.getTitle());
        };

        try {
            emailService.sendEmail(recipient, subject, message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send RSVP email to " + recipient, e);
        }
    }
}

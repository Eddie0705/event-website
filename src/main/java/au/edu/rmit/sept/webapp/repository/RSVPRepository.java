package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.models.RSVP;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RSVPRepository extends JpaRepository<RSVP, Long> {

    Optional<RSVP> findByEventAndUser(Event event, User user);

    boolean existsByEventAndUser(Event event, User user);

    // Custom default method to get RSVP status
    default Optional<RSVP.RSVPStatus> getRsvpStatus(Event event, User user) {
        return findByEventAndUser(event, user)
                .map(RSVP::getRsvpStatus);
    }
}

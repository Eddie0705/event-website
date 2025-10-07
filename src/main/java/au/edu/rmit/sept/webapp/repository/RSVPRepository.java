package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.models.RSVP;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.Event.EventStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RSVPRepository extends JpaRepository<RSVP, Long> {

    Optional<RSVP> findByEventAndUser(Event event, User user);

    boolean existsByEventAndUser(Event event, User user);

    // Custom default method to get RSVP status
    default Optional<RSVP.RSVPStatus> getRsvpStatus(Event event, User user) {
        return findByEventAndUser(event, user)
                .map(RSVP::getRsvpStatus);
    }

    // method to get attendance status
    @Query("SELECT r.attendanceStatus FROM RSVP r WHERE r.event.id = :eventId AND r.user.id = :userId")
    Optional<RSVP.AttendanceStatus> getAttendanceStatus(
            @Param("eventId") Long eventId,
            @Param("userId") Long userId);


    // update attendance status
    @Modifying
    @Query("UPDATE RSVP r SET r.attendanceStatus = :status, r.checkInTime = CURRENT_TIMESTAMP " +
        "WHERE r.event.id = :eventId AND r.user.id = :userId")
    int updateAttendanceStatus(
            @Param("eventId") Long eventId,
            @Param("userId") Long userId,
            @Param("status") RSVP.AttendanceStatus status);

}

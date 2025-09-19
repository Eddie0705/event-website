package au.edu.rmit.sept.webapp.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "rsvps", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"event_id", "user_id"})
})
public class RSVP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rsvpId;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "rsvp_status", nullable = false)
    private RSVPStatus rsvpStatus = RSVPStatus.YES;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false)
    private AttendanceStatus attendanceStatus = AttendanceStatus.NOT_CHECKED;

    private LocalDateTime checkInTime;
    private String qrCode;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // getters + setters

    public Long getRsvpId() {
        return rsvpId;
    }

    public void setRsvpId(Long rsvpId) {
        this.rsvpId = rsvpId;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public RSVPStatus getRsvpStatus() {
        return rsvpStatus;
    }

    public void setRSVPStatus(RSVPStatus rsvpStatus) {
        this.rsvpStatus = rsvpStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public AttendanceStatus getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(AttendanceStatus attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
        this.updatedAt = LocalDateTime.now();
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // constructors
    public RSVP() {
    }

    public RSVP(Event event, User user, RSVPStatus rsvpStatus) {
        this.event = event;
        this.user = user;
        this.rsvpStatus = rsvpStatus;
        this.attendanceStatus = AttendanceStatus.NOT_CHECKED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // enums
    public enum RSVPStatus {
        YES, NO, MAYBE, CANCELLED
    }

    public enum AttendanceStatus {
        NOT_CHECKED, CHECKED_IN, NO_SHOW
    }

}

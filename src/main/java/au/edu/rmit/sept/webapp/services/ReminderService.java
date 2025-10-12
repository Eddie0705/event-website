package au.edu.rmit.sept.webapp.services;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.RSVP;
import au.edu.rmit.sept.webapp.repository.RSVPRepository;


@Service
public class ReminderService {

    @Autowired
    private RSVPRepository rsvpRepository;

    @Autowired
    private EmailService emailService;

    // Runs every hour
    @Scheduled(cron = "0 0 * * * *") 
    //
    public void checkHourlyReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<RSVP> rsvps = rsvpRepository.findAll();

        for (RSVP rsvp : rsvps) {
            Event event  = rsvp.getEvent();

            // check if RSVP status is valid 
            RSVP.RSVPStatus status = rsvp.getRsvpStatus();

            if (status == RSVP.RSVPStatus.cancelled || status == RSVP.RSVPStatus.no) {
                continue; // skip declined RSVPs
            }

            // Combine event date and time
            LocalDateTime eventDateTime = LocalDateTime.of(event.getEventDate(), event.getStartTime());


            long hoursToEvent = Duration.between(now, eventDateTime).toHours();

            // Check if event is 1 hour (or less) and send message
            if (hoursToEvent <= 1.0) {
                sendReminder(rsvp, "Your event is starting in 1 hour!");
            }
        }
    }

    // Runs every day at 12:00 noon
    @Scheduled(cron = "0 0 12 * * *")
    public void checkDailyReminders() {
        LocalDate today = LocalDate.now();
        List<RSVP> rsvps = rsvpRepository.findAll();

        for (RSVP rsvp : rsvps) {
            LocalDate eventDate = rsvp.getEvent().getEventDate();

            long daysToEvent = ChronoUnit.DAYS.between(today, eventDate);
            
            // send message if event is in 20, 14, 7, or 1 days
            if (daysToEvent == 20 || daysToEvent == 14 || daysToEvent == 7 || daysToEvent == 1) {
                sendReminder(rsvp, "Reminder: " + daysToEvent + " days until your event!");
            }
        }
    }

    private void sendReminder(RSVP rsvp, String customMessage) {
        Event event = rsvp.getEvent();
        String recipient = rsvp.getUser().getEmail();

        String subject = "Reminder: " + event.getTitle() + " is coming up";

        String body = String.format(
            "Hello %s,\n\n" +
            "%s\n\n" +
            "Event Details:\n" +
            "• Title: %s\n" +
            "• Date: %s\n" +
            "• Time: %s - %s\n" +
            "• Location: %s\n\n" +
            "We look forward to seeing you there!\n\n" +
            "Best regards,\n" +
            "The Events Team",
            rsvp.getUser().getFullName(), 
            customMessage,
            event.getTitle(),
            event.getEventDate(),
            event.getStartTime(),
            event.getEndTime(),
            event.getLocation()
        );

        try {
            emailService.sendEmail(recipient, subject, body);
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to send reminder to " + recipient + " for event '" + event.getTitle() + "'", e);
        }

    }


    private void sendMsg(RSVP rsvp,  String subject, String customMessage) {
        Event event = rsvp.getEvent();
        String recipient = rsvp.getUser().getEmail();

    

        try {
            emailService.sendEmail(recipient, subject, customMessage);
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to send reminder to " + recipient + " for event '" + event.getTitle() + "'", e);
        }
    }
}

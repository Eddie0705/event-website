package au.edu.rmit.sept.webapp.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotBlank(message = "Location is required")
    @Size(max = 500, message = "Location must not exceed 500 characters")
    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "venue_details", columnDefinition = "TEXT")
    private String venueDetails;

    @NotNull(message = "Maximum capacity is required")
    @Min(value = 1, message = "Maximum capacity must be at least 1")
    @Max(value = 10000, message = "Maximum capacity cannot exceed 10,000")
    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(name = "current_rsvp_count", nullable = false)
    private Integer currentRsvpCount = 0;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "club_id")
    private Long clubId;

    @NotNull(message = "Creator is required")
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Size(max = 500, message = "Cover image path must not exceed 500 characters")
    @Column(name = "cover_image")
    private String coverImage;

    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Size(max = 3, message = "Currency code must be 3 characters")
    @Column(name = "currency", length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)

    private EventStatus status = EventStatus.draft;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Size(max = 1000, message = "Google Maps URL must not exceed 1000 characters")
    @Column(name = "google_maps_url")
    private String googleMapsUrl;

    @Size(max = 500, message = "QR code data must not exceed 500 characters")
    @Column(name = "qr_code_data")
    private String qrCodeData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Enum for event status
    public enum EventStatus {

            draft,
        published,
        cancelled,
        completed
    }


    // Default constructor
    public Event() {
    }


            LocalTime startTime, LocalTime endTime, String location,
            Integer maxCapacity, Long createdBy) {
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.maxCapacity = maxCapacity;
        this.createdBy = createdBy;
    }

    // Custom validation method
    @AssertTrue(message = "End time must be after start time")
    public boolean isEndTimeAfterStartTime() {
        if (startTime == null || endTime == null) {
            return true; // Let @NotNull handle null validation
        }
        return endTime.isAfter(startTime);
    }

    @AssertTrue(message = "Price and currency are required for paid events")
    public boolean isPaidEventValid() {
        if (isPaid != null && isPaid) {
            return price != null && price.compareTo(BigDecimal.ZERO) > 0 &&
                    currency != null && !currency.trim().isEmpty();
        }
        return true;
    }

    @AssertTrue(message = "Registration deadline must be before event date")
    public boolean isRegistrationDeadlineValid() {
        if (registrationDeadline == null || eventDate == null) {
            return true;
        }
        return registrationDeadline.toLocalDate().isBefore(eventDate) ||
                registrationDeadline.toLocalDate().isEqual(eventDate);
    }

    // Getters and Setters
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVenueDetails() {
        return venueDetails;
    }

    public void setVenueDetails(String venueDetails) {
        this.venueDetails = venueDetails;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Integer getCurrentRsvpCount() {
        return currentRsvpCount;
    }

    public void setCurrentRsvpCount(Integer currentRsvpCount) {
        this.currentRsvpCount = currentRsvpCount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public LocalDateTime getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(LocalDateTime registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public String getGoogleMapsUrl() {
        return googleMapsUrl;
    }

    public void setGoogleMapsUrl(String googleMapsUrl) {
        this.googleMapsUrl = googleMapsUrl;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
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

    // Utility methods
    public boolean isAvailable() {
        return currentRsvpCount < maxCapacity;
    }

    public int getAvailableSpots() {
        return maxCapacity - currentRsvpCount;
    }

    public boolean isRegistrationOpen() {
        if (registrationDeadline == null) {
            return status == EventStatus.published;
            return status == EventStatus.published;
        }
        return status == EventStatus.published &&

                LocalDateTime.now().isBefore(registrationDeadline);
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + eventId +
                ", title='" + title + '\'' +
                ", eventDate=" + eventDate +
                ", location='" + location + '\'' +
                ", status=" + status +
                '}';
    }
}
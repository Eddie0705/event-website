package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.Event.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

        // Find events by organiser (created_by)
        List<Event> findByCreatedByOrderByEventDateDesc(Long createdBy);

        Page<Event> findByCreatedByOrderByEventDateDesc(Long createdBy, Pageable pageable);

        // Find published events
        List<Event> findByStatusOrderByEventDateAsc(EventStatus status);

        Page<Event> findByStatusOrderByEventDateAsc(EventStatus status, Pageable pageable);

        // Find featured events
        List<Event> findByIsFeaturedTrueAndStatusOrderByEventDateAsc(EventStatus status);

        // Find event by ID
        Optional<Event> findByEventId(Long eventId);

        // Find upcoming events (published and future dates)
        @Query("SELECT e FROM Event e WHERE e.status = :status AND e.eventDate >= :currentDate ORDER BY e.eventDate ASC")
        List<Event> findUpcomingEvents(@Param("status") EventStatus status,
                        @Param("currentDate") LocalDate currentDate);

        @Query("SELECT e FROM Event e WHERE e.status = :status AND e.eventDate >= :currentDate ORDER BY e.eventDate ASC")
        Page<Event> findUpcomingEvents(@Param("status") EventStatus status, @Param("currentDate") LocalDate currentDate,
                        Pageable pageable);

        // Find events by category
        List<Event> findByCategoryIdAndStatusOrderByEventDateAsc(Long categoryId, EventStatus status);

        // Find events by club
        List<Event> findByClubIdAndStatusOrderByEventDateAsc(Long clubId, EventStatus status);

        // Find events by date range
        @Query("SELECT e FROM Event e WHERE e.status = :status AND e.eventDate BETWEEN :startDate AND :endDate ORDER BY e.eventDate ASC")
        List<Event> findEventsByDateRange(@Param("status") EventStatus status,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Search events by title or description
        @Query("SELECT e FROM Event e WHERE e.status = :status AND " +
                        "(LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                        "ORDER BY e.eventDate ASC")
        List<Event> searchEvents(@Param("status") EventStatus status, @Param("searchTerm") String searchTerm);

        @Query("SELECT e FROM Event e WHERE e.status = :status AND " +
                        "(LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                        "ORDER BY e.eventDate ASC")
        Page<Event> searchEvents(@Param("status") EventStatus status, @Param("searchTerm") String searchTerm,
                        Pageable pageable);

        // Find events with available spots
        @Query("SELECT e FROM Event e WHERE e.status = :status AND e.currentRsvpCount < e.maxCapacity ORDER BY e.eventDate ASC")
        List<Event> findEventsWithAvailableSpots(@Param("status") EventStatus status);

        // Find events by location
        @Query("SELECT e FROM Event e WHERE e.status = :status AND LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%')) ORDER BY e.eventDate ASC")
        List<Event> findEventsByLocation(@Param("status") EventStatus status, @Param("location") String location);

        // Find events with registration deadline approaching
        @Query("SELECT e FROM Event e WHERE e.status = :status AND e.registrationDeadline IS NOT NULL AND " +
                        "e.registrationDeadline BETWEEN :now AND :deadline ORDER BY e.registrationDeadline ASC")
        List<Event> findEventsWithUpcomingDeadline(@Param("status") EventStatus status,
                        @Param("now") LocalDateTime now,
                        @Param("deadline") LocalDateTime deadline);

        // Find past events
        @Query("SELECT e FROM Event e WHERE e.eventDate < :currentDate ORDER BY e.eventDate DESC")
        List<Event> findPastEvents(@Param("currentDate") LocalDate currentDate);

        @Query("SELECT e FROM Event e WHERE e.eventDate < :currentDate ORDER BY e.eventDate DESC")
        Page<Event> findPastEvents(@Param("currentDate") LocalDate currentDate, Pageable pageable);

        // Find events for today
        List<Event> findByEventDateAndStatusOrderByStartTimeAsc(LocalDate eventDate, EventStatus status);

        // Count methods
        long countByCreatedBy(Long createdBy);

        long countByStatus(EventStatus status);

        long countByCreatedByAndStatus(Long createdBy, EventStatus status);

        @Query("SELECT COUNT(e) FROM Event e WHERE e.status = :status AND e.eventDate >= :currentDate")
        long countUpcomingEvents(@Param("status") EventStatus status, @Param("currentDate") LocalDate currentDate);

        // Check if event exists by title and date (to prevent duplicates)
        boolean existsByTitleAndEventDateAndCreatedBy(String title, LocalDate eventDate, Long createdBy);

        // Find events by multiple categories
        @Query("SELECT e FROM Event e WHERE e.status = :status AND e.categoryId IN :categoryIds ORDER BY e.eventDate ASC")
        List<Event> findEventsByCategories(@Param("status") EventStatus status,
                        @Param("categoryIds") List<Long> categoryIds);

        // Find free events
        @Query("SELECT e FROM Event e WHERE e.status = :status AND (e.isPaid = false OR e.isPaid IS NULL) ORDER BY e.eventDate ASC")
        List<Event> findFreeEvents(@Param("status") EventStatus status);

        // Find paid events
        @Query("SELECT e FROM Event e WHERE e.status = :status AND e.isPaid = true ORDER BY e.eventDate ASC")
        List<Event> findPaidEvents(@Param("status") EventStatus status);

        // Find events by price range
        @Query("SELECT e FROM Event e WHERE e.status = :status AND e.isPaid = true AND " +
                        "e.price BETWEEN :minPrice AND :maxPrice ORDER BY e.eventDate ASC")
        List<Event> findEventsByPriceRange(@Param("status") EventStatus status,
                        @Param("minPrice") java.math.BigDecimal minPrice,
                        @Param("maxPrice") java.math.BigDecimal maxPrice);

        // Custom query for dashboard statistics
        @Query("SELECT " +
                        "COUNT(CASE WHEN e.status = 'PUBLISHED' THEN 1 END) as published, " +
                        "COUNT(CASE WHEN e.status = 'DRAFT' THEN 1 END) as draft, " +
                        "COUNT(CASE WHEN e.eventDate >= CURRENT_DATE THEN 1 END) as upcoming, " +
                        "COUNT(CASE WHEN e.eventDate < CURRENT_DATE THEN 1 END) as past " +
                        "FROM Event e WHERE e.createdBy = :createdBy")
        Object[] getEventStatistics(@Param("createdBy") Long createdBy);

        // Find events that need QR code generation
        @Query("SELECT e FROM Event e WHERE e.status = 'PUBLISHED' AND (e.qrCodeData IS NULL OR e.qrCodeData = '')")
        List<Event> findEventsNeedingQrCode();

        // Update event status directly (bypasses validation)
        @Modifying
        @Query("UPDATE Event e SET e.status = :status WHERE e.eventId = :eventId")
        int updateEventStatus(@Param("eventId") Long eventId, @Param("status") EventStatus status);

        // Update QR code data directly (bypasses validation)
        @Modifying
        @Query("UPDATE Event e SET e.qrCodeData = :qrCodeData WHERE e.eventId = :eventId")
        int updateQrCodeData(@Param("eventId") Long eventId, @Param("qrCodeData") String qrCodeData);

        // Delete events created by a specific user
        @Modifying
        @Query("DELETE FROM Event e WHERE e.createdBy = :createdBy")
        void deleteByCreatedBy(@Param("createdBy") Long createdBy);

        // Advanced search with multiple optional filters
        @Query("""
                        SELECT e FROM Event e
                        WHERE e.status = 'PUBLISHED'
                        AND (
                            :search IS NULL OR :search = ''
                            OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))
                        )
                        AND (
                            :locations IS NULL OR e.location IN :locations
                        )
                        AND (
                            :date IS NULL OR :date = '' OR e.eventDate = CAST(:date AS date)
                        )
                        ORDER BY e.eventDate ASC
                        """)
        List<Event> searchEventsWithLocations(
                        @Param("search") String search,
                        @Param("date") String date,
                        @Param("locations") List<String> locations);

        // Advanced search with single location filter
        @Query("""
                            SELECT e FROM Event e
                            WHERE e.status = 'PUBLISHED'
                            AND (
                                :search IS NULL OR :search = ''
                                OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%'))
                                OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))
                            )
                            AND (
                                :location IS NULL OR :location = ''
                                OR LOWER(e.location) LIKE LOWER(CONCAT('%', TRIM(:location), '%'))
                            )
                            AND (
                                :date IS NULL OR :date = ''
                                OR e.eventDate = CAST(:date AS date)
                            )
                            ORDER BY e.eventDate ASC
                        """)
        List<Event> searchEventsBasic(
                        @Param("search") String search,
                        @Param("date") String date,
                        @Param("location") String location);

}
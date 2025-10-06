package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import au.edu.rmit.sept.webapp.services.EventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@Controller
public class CalendarController {

    @Autowired
    private EventService eventService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    // -------- Helpers --------
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            // Your Custom principal exposes the User
            CustomUserDetailsService.CustomUserPrincipal principal =
                    (CustomUserDetailsService.CustomUserPrincipal) auth.getPrincipal();
            return principal.getUser();
        }
        return null;
    }

    // -------- Route --------
    @GetMapping("/calendar")
    public String showCalendar(
            @RequestParam(value = "view", defaultValue = "month") String view,
            @RequestParam(value = "date", required = false) String dateIso,
            Model model
    ) {
        // Current user (used by your base.html for nav/profile)
        User currentUser = getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);

        // Anchor date (defaults to today if not provided / invalid)
        LocalDate today = LocalDate.now();
        LocalDate anchorDate = today;
        if (dateIso != null && !dateIso.isBlank()) {
            try {
                anchorDate = LocalDate.parse(dateIso);
            } catch (DateTimeParseException ignored) {
                anchorDate = today;
            }
        }

        // Build calendar data (range + grid days)
        CalendarData calendarData = buildCalendarData(anchorDate, view);

        // Fetch events limited to the visible grid window
        LocalDate rangeStart;
        LocalDate rangeEnd;
        if ("week".equalsIgnoreCase(view)) {
            rangeStart = calendarData.getWeekStart();
            rangeEnd = calendarData.getWeekEnd();
        } else {
            // Use full grid so lead/trail days show events too
            List<LocalDate> days = calendarData.getMonthDays();
            rangeStart = days.isEmpty() ? anchorDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) : days.get(0);
            rangeEnd = days.isEmpty() ? anchorDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)) : days.get(days.size() - 1);
        }

        List<Event> visibleEvents = eventService.getEventsByDateRange(rangeStart, rangeEnd);

        // Group events by LocalDate for Thymeleaf
        Map<LocalDate, List<Event>> eventsByDate = new HashMap<>();
        for (Event e : visibleEvents) {
            LocalDate d = e.getEventDate();
            eventsByDate.computeIfAbsent(d, k -> new ArrayList<>()).add(e);
        }

        // Also provide a String-keyed map to avoid any template key-equality issues
        DateTimeFormatter isoDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, List<Event>> eventsByDateStr = visibleEvents.stream()
                .collect(Collectors.groupingBy(e -> e.getEventDate().format(isoDate)));

        // Prev/next anchors for nav
        LocalDate prevDate;
        LocalDate nextDate;
        if ("week".equalsIgnoreCase(view)) {
            prevDate = calendarData.getWeekStart().minusWeeks(1);
            nextDate = calendarData.getWeekStart().plusWeeks(1);
        } else {
            YearMonth ym = YearMonth.from(calendarData.getMonthStart());
            prevDate = ym.minusMonths(1).atDay(1);
            nextDate = ym.plusMonths(1).atDay(1);
        }

        // Model for calendar.html
        model.addAttribute("view", view.toLowerCase());
        model.addAttribute("today", today);
        model.addAttribute("anchorDate", anchorDate);
        model.addAttribute("calendarData", calendarData);
        model.addAttribute("eventsByDate", eventsByDate);
        model.addAttribute("eventsByDateStr", eventsByDateStr);
        model.addAttribute("prevDate", prevDate);
        model.addAttribute("nextDate", nextDate);
        model.addAttribute("yearMonth",
                "week".equalsIgnoreCase(view)
                        ? YearMonth.from(calendarData.getWeekStart())
                        : YearMonth.from(calendarData.getMonthStart())
        );

        // Your template file is /templates/calendar/calendar.html
        return "calendar/calendar";
    }

    // -------- Calendar builder --------
    private CalendarData buildCalendarData(LocalDate anchorDate, String view) {
        CalendarData data = new CalendarData();

        if ("week".equalsIgnoreCase(view)) {
            LocalDate weekStart = anchorDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate weekEnd = weekStart.plusDays(6);

            List<LocalDate> weekDays = new ArrayList<>();
            for (int i = 0; i < 7; i++) weekDays.add(weekStart.plusDays(i));

            data.setWeekStart(weekStart);
            data.setWeekEnd(weekEnd);
            data.setWeekDays(weekDays);

            // also set monthStart/End so template's yearMonth calc is simple
            data.setMonthStart(weekStart);
            data.setMonthEnd(weekEnd);
            return data;
        }

        // Month view
        YearMonth ym = YearMonth.from(anchorDate);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();

        // Build a Monday-to-Sunday grid that covers the full month
        LocalDate gridStart = monthStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate gridEnd = monthEnd.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<LocalDate> monthDays = new ArrayList<>();
        for (LocalDate d = gridStart; !d.isAfter(gridEnd); d = d.plusDays(1)) {
            monthDays.add(d);
        }

        data.setMonthDays(monthDays);
        data.setMonthStart(monthStart);
        data.setMonthEnd(monthEnd);
        return data;
    }

    // -------- POJO for Thymeleaf --------
    public static class CalendarData {
        private LocalDate weekStart;
        private LocalDate weekEnd;
        private List<LocalDate> weekDays;

        private List<LocalDate> monthDays;
        private LocalDate monthStart;
        private LocalDate monthEnd;

        public LocalDate getWeekStart() { return weekStart; }
        public void setWeekStart(LocalDate weekStart) { this.weekStart = weekStart; }

        public LocalDate getWeekEnd() { return weekEnd; }
        public void setWeekEnd(LocalDate weekEnd) { this.weekEnd = weekEnd; }

        public List<LocalDate> getWeekDays() { return weekDays; }
        public void setWeekDays(List<LocalDate> weekDays) { this.weekDays = weekDays; }

        public List<LocalDate> getMonthDays() { return monthDays; }
        public void setMonthDays(List<LocalDate> monthDays) { this.monthDays = monthDays; }

        public LocalDate getMonthStart() { return monthStart; }
        public void setMonthStart(LocalDate monthStart) { this.monthStart = monthStart; }

        public LocalDate getMonthEnd() { return monthEnd; }
        public void setMonthEnd(LocalDate monthEnd) { this.monthEnd = monthEnd; }
    }
}

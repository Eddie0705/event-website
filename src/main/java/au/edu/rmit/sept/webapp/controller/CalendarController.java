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
import java.util.*;

@Controller
public class CalendarController {

    @Autowired private EventService eventService;
    @Autowired private CustomUserDetailsService userDetailsService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            CustomUserDetailsService.CustomUserPrincipal principal =
                    (CustomUserDetailsService.CustomUserPrincipal) auth.getPrincipal();
            return principal.getUser();
        }
        return null;
    }

    @GetMapping("/calendar")
    public String calendar(
            @RequestParam(value = "view", defaultValue = "month") String view,
            @RequestParam(value = "date", required = false) String dateIso,
            Model model) {

        User currentUser = getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);

        LocalDate today = LocalDate.now();
        LocalDate anchorDate = (dateIso == null || dateIso.isBlank())
                ? today
                : LocalDate.parse(dateIso); // yyyy-MM-dd

        List<Event> events = eventService.getPublishedEvents();

        // build calendar data
        CalendarData calendarData = generateCalendarData(anchorDate, view);
        List<Event> visibleEvents;
        if ("week".equalsIgnoreCase(view)) {
            visibleEvents = filter(events, calendarData.getWeekStart(), calendarData.getWeekEnd());
        } else {
            visibleEvents = filter(events, calendarData.getMonthStart(), calendarData.getMonthEnd());
        }

        Map<LocalDate, List<Event>> eventsByDate = new HashMap<>();
        for (Event e : visibleEvents) {
            if (e.getEventDate() != null) {
                eventsByDate.computeIfAbsent(e.getEventDate(), k -> new ArrayList<>()).add(e);
            }
        }

        LocalDate prevDate, nextDate;
        if ("week".equalsIgnoreCase(view)) {
            prevDate = calendarData.getWeekStart().minusWeeks(1);
            nextDate = calendarData.getWeekStart().plusWeeks(1);
        } else {
            YearMonth ym = YearMonth.from(calendarData.getMonthStart());
            prevDate = ym.minusMonths(1).atDay(1);
            nextDate = ym.plusMonths(1).atDay(1);
        }

        model.addAttribute("view", view.toLowerCase());
        model.addAttribute("today", today);
        model.addAttribute("anchorDate", anchorDate);
        model.addAttribute("calendarData", calendarData);
        model.addAttribute("eventsByDate", eventsByDate);
        model.addAttribute("prevDate", prevDate);
        model.addAttribute("nextDate", nextDate);
        model.addAttribute("yearMonth", "week".equalsIgnoreCase(view)
                ? YearMonth.from(calendarData.getWeekStart())
                : YearMonth.from(calendarData.getMonthStart()));

        return "calendar/calendar";
    }

    private List<Event> filter(List<Event> all, LocalDate start, LocalDate end) {
        List<Event> out = new ArrayList<>();
        for (Event e : all) {
            LocalDate d = e.getEventDate();
            if (d != null && !d.isBefore(start) && !d.isAfter(end)) {
                out.add(e);
            }
        }
        return out;
    }

    private CalendarData generateCalendarData(LocalDate anchorDate, String view) {
        CalendarData data = new CalendarData();

        if ("week".equalsIgnoreCase(view)) {
            LocalDate weekStart = anchorDate.with(DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(6);

            List<LocalDate> weekDays = new ArrayList<>();
            for (int i = 0; i < 7; i++) weekDays.add(weekStart.plusDays(i));

            data.setWeekStart(weekStart);
            data.setWeekEnd(weekEnd);
            data.setWeekDays(weekDays);
            data.setMonthStart(weekStart);
            data.setMonthEnd(weekEnd);
            return data;
        }

        YearMonth ym = YearMonth.from(anchorDate);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();

        LocalDate gridStart = monthStart.with(DayOfWeek.MONDAY);
        if (gridStart.isAfter(monthStart)) gridStart = gridStart.minusWeeks(1);
        LocalDate gridEnd = monthEnd.with(DayOfWeek.SUNDAY);
        if (gridEnd.isBefore(monthEnd)) gridEnd = gridEnd.plusWeeks(1);

        List<LocalDate> monthDays = new ArrayList<>();
        for (LocalDate d = gridStart; !d.isAfter(gridEnd); d = d.plusDays(1)) {
            monthDays.add(d);
        }

        data.setMonthDays(monthDays);
        data.setMonthStart(monthStart);
        data.setMonthEnd(monthEnd);
        return data;
    }

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

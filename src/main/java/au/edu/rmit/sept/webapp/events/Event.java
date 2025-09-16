package au.edu.rmit.sept.webapp.events;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class Event {
    private final long id;
    private final String title;
    private final String description;
    private final LocalDate date;
    private final LocalTime time;
    private final String location;
    private final String category;
    private final List<String> tags;

    public Event(long id,
                 String title,
                 String description,
                 LocalDate date,
                 LocalTime time,
                 String location,
                 String category,
                 List<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.category = category;
        this.tags = List.copyOf(tags);
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
    public List<String> getTags() { return tags; }
}




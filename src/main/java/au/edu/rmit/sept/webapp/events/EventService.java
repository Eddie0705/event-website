package au.edu.rmit.sept.webapp.events;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EventService {

    private final AtomicLong idSequence = new AtomicLong(1);
    private final CopyOnWriteArrayList<Event> events = new CopyOnWriteArrayList<>();

    public Event create(EventForm form) {
        List<String> normalizedTags = normalizeTags(form.getTags());
        long id = idSequence.getAndIncrement();
        Event event = new Event(
                id,
                form.getTitle(),
                form.getDescription(),
                form.getDate(),
                form.getTime(),
                form.getLocation(),
                form.getCategory(),
                normalizedTags
        );
        events.add(event);
        return event;
    }

    public List<Event> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(events));
    }

    private List<String> normalizeTags(String tags) {
        if (tags == null) {
            return List.of();
        }
        String[] parts = tags.split(",");
        Set<String> uniqueOrdered = new LinkedHashSet<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                uniqueOrdered.add(trimmed);
            }
        }
        return new ArrayList<>(uniqueOrdered);
    }
}




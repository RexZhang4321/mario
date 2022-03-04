package observers.events;

public class Event {

    public EventType eventType;

    public Event() {
        this(EventType.UserEvent);
    }

    public Event(EventType eventType) {
        this.eventType = eventType;
    }
}

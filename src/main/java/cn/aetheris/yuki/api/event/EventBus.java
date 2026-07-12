package cn.aetheris.yuki.api.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class EventBus {

    private static final Logger LOGGER = Logger.getLogger(EventBus.class.getName());

    private final Map<Class<?>, CopyOnWriteArraySet<Consumer<?>>> subscribers = new ConcurrentHashMap<>();

    public <E> Subscription subscribe(Class<E> eventType, Consumer<E> consumer) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArraySet<>()).add(consumer);
        return new Subscription(eventType, consumer);
    }

    public void unsubscribe(Subscription subscription) {
        CopyOnWriteArraySet<Consumer<?>> set = subscribers.get(subscription.eventType());
        if (set != null) {
            set.remove(subscription.consumer());
        }
    }

    public void clear() {
        subscribers.clear();
    }

    @SuppressWarnings("unchecked")
    public <E> void publish(E event) {
        CopyOnWriteArraySet<Consumer<?>> set = subscribers.get(event.getClass());
        if (set == null || set.isEmpty()) return;
        for (Consumer<?> consumer : set) {
            try {
                ((Consumer<E>) consumer).accept(event);
            } catch (Exception t) {
                LOGGER.warning(
                    "EventBus subscriber threw exception for " + event.getClass().getSimpleName() + ": " + t.getMessage());
            }
        }
    }

    public int getSubscriberCount(Class<?> eventType) {
        CopyOnWriteArraySet<Consumer<?>> set = subscribers.get(eventType);
        return set == null ? 0 : set.size();
    }

    public record Subscription(Class<?> eventType, Consumer<?> consumer) {}
}

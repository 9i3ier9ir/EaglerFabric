package net.lax1dude.eaglercraft.mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lightweight event bus for Fabric-like mod hooks. */
public final class ModEventBus {
    public interface Listener {
        void onEvent(String modId, String[] args);
    }

    private final Map<String, List<Listener>> listeners = new LinkedHashMap<>();

    public synchronized void registerListener(String eventName, Listener listener) {
        listeners.computeIfAbsent(eventName, ignored -> new ArrayList<>()).add(listener);
    }

    public synchronized void unregisterListener(String eventName, Listener listener) {
        List<Listener> list = listeners.get(eventName);
        if (list != null) {
            list.remove(listener);
            if (list.isEmpty()) {
                listeners.remove(eventName);
            }
        }
    }

    public synchronized List<Listener> getListeners(String eventName) {
        List<Listener> list = listeners.get(eventName);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    public synchronized void emit(String eventName, String modId, String[] args) {
        List<Listener> list = listeners.get(eventName);
        if (list == null) {
            return;
        }
        for (Listener listener : new ArrayList<>(list)) {
            listener.onEvent(modId, args == null ? new String[0] : args);
        }
    }
}

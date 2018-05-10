package com.haulmont.addon.globalevents;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class LocalRegistry {

    private static final LocalRegistry instance = new LocalRegistry();

    private List<Consumer<byte[]>> listeners = new CopyOnWriteArrayList<>();

    public static LocalRegistry getInstance() {
        return instance;
    }

    public void addListener(Consumer<byte[]> listener) {
        listeners.add(listener);
    }

    public void notifyListeners(byte[] message) {
        for (Consumer<byte[]> listener : listeners) {
            listener.accept(message);
        }
    }
}

package sol.awakeapi.api;

import sol.awakeapi.api.interfaces.AIQueryHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AwakeApiQueryReg {
    private static final Map<UUID, AIQueryHandler> callbacks = new ConcurrentHashMap<>();

    public static UUID registerCallback(AIQueryHandler callback) {
        UUID id = UUID.randomUUID();
        callbacks.put(id, callback);
        return id;
    }

    public static AIQueryHandler getCallback(UUID id) {
        return callbacks.get(id);
    }

    public static void removeCallback(UUID id) {
        callbacks.remove(id);
    }
}


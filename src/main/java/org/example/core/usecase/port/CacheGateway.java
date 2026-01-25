package org.example.core.usecase.port;
// Порт для работы с кэшем (сохранение и получение данных)

public interface CacheGateway {
    byte[] get(String key);
    void put(String key, byte[] data);
}

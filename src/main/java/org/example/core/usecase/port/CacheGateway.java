package org.example.core.usecase.port;

public interface CacheGateway {
    byte[] get(String key);
    void put(String key, byte[] data);
}

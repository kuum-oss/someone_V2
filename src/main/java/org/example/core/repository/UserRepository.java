package org.example.core.repository;

import org.example.core.entity.User;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Integer id);
    void updatePoints(Integer userId, int points);
    void deleteById(Integer id);
    void addToBlacklist(String email, String reason);
    boolean isBlacklisted(String email);
    java.util.List<org.example.core.entity.User> findAll();
}

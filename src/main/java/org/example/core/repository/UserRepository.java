package org.example.core.repository;

import org.example.core.entity.User;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Integer id);
    void updatePoints(Integer userId, int points);
}

package org.example.core.service;

import org.example.core.entity.User;
import org.example.core.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private User currentUser;
    // Сервис аутентификации и управления текущим пользователем
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // Кодировщик паролей (BCrypt)
    }

    public boolean register(String email, String password, boolean isAdmin) {
        if (userRepository.findByEmail(email).isPresent()) {
            return false;
        }
        String encodedPassword = passwordEncoder.encode(password); // Хэшируем пароль перед сохранением
        userRepository.save(new User(null, email, encodedPassword, isAdmin, 5));
        // Создаём и сохраняем нового пользователя
        // points = 5 — стартовые очки
        return true;
    }

    public boolean login(String email, String password) {
        if (userRepository.isBlacklisted(email)) {
            throw new SecurityException("Ваш аккаунт заблоковано адміном.");
        }
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            this.currentUser = userOpt.get();
            return true;
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void updateCurrentUserPoints(int newPoints) {
        if (currentUser != null) {
            userRepository.updatePoints(currentUser.getId(), newPoints);
            currentUser.setPoints(newPoints);
        }
    }
    
    public boolean isAuthenticated() {
        return currentUser != null;
    }
}

package org.example.application.controller;

import org.example.application.state.LibraryViewState;
import org.example.core.service.AuthService;

public class AuthController {
    private final AuthService authService;
    private final LibraryViewState state;

    public AuthController(AuthService authService, LibraryViewState state) {
        this.authService = authService;
        this.state = state;
    }

    public boolean isAuthenticated() {
        return authService.isAuthenticated();
    }

    public void login(String email, String password) {
        if (authService.login(email, password)) {
            state.setCurrentUser(authService.getCurrentUser());
        }
    }

    public void logout() {
        authService.logout();
        state.setCurrentUser(null);
    }

    public void updatePoints(int points) {
        authService.updateCurrentUserPoints(points);
        state.setCurrentUser(authService.getCurrentUser());
    }

    public void syncState() {
        state.setCurrentUser(authService.getCurrentUser());
    }
}

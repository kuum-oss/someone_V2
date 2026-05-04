package org.example.core.repository;

import org.example.core.entity.LibrarySettings;

public interface LibrarySettingsRepository {
    LibrarySettings getSettings();
    void updateSettings(LibrarySettings settings);
}

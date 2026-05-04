package org.example.infrastructure.repository;

import org.example.core.entity.LibrarySettings;
import org.example.core.repository.LibrarySettingsRepository;
import org.example.infrastructure.db.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcLibrarySettingsRepository implements LibrarySettingsRepository {

    @Override
    public LibrarySettings getSettings() {
        String sql = "SELECT total_seats, seats_layout, default_duration_hours, available_periods FROM library_settings WHERE id = 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new LibrarySettings(
                        rs.getInt("total_seats"),
                        rs.getString("seats_layout"),
                        rs.getInt("default_duration_hours"),
                        rs.getString("available_periods")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new LibrarySettings(20, null, 2, "1,2,4,8,24"); // Default
    }

    @Override
    public void updateSettings(LibrarySettings settings) {
        String sql = "UPDATE library_settings SET total_seats = ?, seats_layout = ?, default_duration_hours = ?, available_periods = ? WHERE id = 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, settings.getTotalSeats());
            ps.setString(2, settings.getSeatsLayout());
            ps.setInt(3, settings.getDefaultDurationHours());
            ps.setString(4, settings.getAvailablePeriods());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

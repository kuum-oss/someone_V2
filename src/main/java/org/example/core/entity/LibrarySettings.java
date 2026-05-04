package org.example.core.entity;

public class LibrarySettings {
    private int totalSeats;
    private String seatsLayout;
    private int defaultDurationHours;
    private String availablePeriods; // Comma separated list of hours, e.g. "1,2,4,8,24"

    public LibrarySettings(int totalSeats, String seatsLayout, int defaultDurationHours, String availablePeriods) {
        this.totalSeats = totalSeats;
        this.seatsLayout = seatsLayout;
        this.defaultDurationHours = defaultDurationHours;
        this.availablePeriods = availablePeriods;
    }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public String getSeatsLayout() { return seatsLayout; }
    public void setSeatsLayout(String seatsLayout) { this.seatsLayout = seatsLayout; }

    public int getDefaultDurationHours() { return defaultDurationHours; }
    public void setDefaultDurationHours(int defaultDurationHours) { this.defaultDurationHours = defaultDurationHours; }

    public String getAvailablePeriods() { return availablePeriods; }
    public void setAvailablePeriods(String availablePeriods) { this.availablePeriods = availablePeriods; }
}

package com.eventbooking.tickets.dto;

public record OrganizerEventStatisticsDto(
        Long eventId,
        String eventName,
        Long totalTickets,
        Double totalRevenue,
        Long bookingCount) {

    public OrganizerEventStatisticsDto {
        totalTickets = totalTickets == null ? 0L : totalTickets;
        totalRevenue = totalRevenue == null ? 0.0 : totalRevenue;
    }
}
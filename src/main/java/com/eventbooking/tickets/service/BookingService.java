package com.eventbooking.tickets.service;

import com.eventbooking.tickets.dto.BookingCreateDto;
import com.eventbooking.tickets.entity.Booking;
import com.eventbooking.tickets.entity.Event;
import com.eventbooking.tickets.entity.User;
import com.eventbooking.tickets.repository.BookingRepository;
import org.springframework.stereotype.Service;
import com.eventbooking.tickets.repository.EventRepository;
import com.eventbooking.tickets.repository.UserRepository;
import com.eventbooking.tickets.dto.OrganizerEventStatisticsDto;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
@Service
public class BookingService {
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public BookingService(EventRepository eventRepository, BookingRepository bookingRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    public String createBooking(BookingCreateDto bookingCreateDto) {

        Event event = eventRepository
                .findById(bookingCreateDto.getEventId())
                .orElse(null);

        if (event == null) {
            return "Event not found";
        }

        double totalAmount =
                event.getTicketPrice() * bookingCreateDto.getNumberOfTickets();

        Booking booking = new Booking();

        booking.setEventId(event.getId());
        booking.setNumberOfTickets(bookingCreateDto.getNumberOfTickets());
        booking.setTotalAmount(totalAmount);
        booking.setBookingDate("2026-09-04");
        booking.setBookingStatus("CONFIRMED");

        bookingRepository.save(booking);

        return "Booking successful";
    }
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }
    public String cancelBooking(Long id) {

        Booking booking = bookingRepository.findById(id).orElse(null);

        if (booking == null) {
            return "Booking not found";
        }

        bookingRepository.deleteById(id);

        return "Booking cancelled successfully";
    }

    public List<OrganizerEventStatisticsDto> getOrganizerStatistics(String authenticatedEmail) {
        User organizer = userRepository.findByEmail(authenticatedEmail)
                .filter(user -> "EVENT_ORGANIZER".equals(user.getRole()))
                .orElseThrow(() -> new AccessDeniedException("An event organizer account is required."));

        return bookingRepository.findStatisticsForOrganizer(organizer.getId());
    }

}


package com.eventbooking.tickets;

import com.eventbooking.tickets.entity.Booking;
import com.eventbooking.tickets.entity.Event;
import com.eventbooking.tickets.entity.User;
import com.eventbooking.tickets.repository.BookingRepository;
import com.eventbooking.tickets.repository.EventRepository;
import com.eventbooking.tickets.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrganizerStatisticsIntegrationTests {

    private static final String ORGANIZER_A_EMAIL = "organizer-a-stats@example.test";
    private static final String ORGANIZER_B_EMAIL = "organizer-b-stats@example.test";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Event organizerAEvent;
    private Event organizerAEventWithoutBookings;

    @BeforeEach
    void setUp() {
        User organizerA = userRepository.save(createUser(ORGANIZER_A_EMAIL, "EVENT_ORGANIZER"));
        User organizerB = userRepository.save(createUser(ORGANIZER_B_EMAIL, "EVENT_ORGANIZER"));

        organizerAEvent = eventRepository.saveAndFlush(createEvent("Organizer A event", organizerA));
        organizerAEventWithoutBookings = eventRepository.saveAndFlush(createEvent("Organizer A empty event", organizerA));
        Event organizerBEvent = eventRepository.saveAndFlush(createEvent("Organizer B event", organizerB));

        saveBooking(organizerAEvent, 2, 5000.0);
        saveBooking(organizerAEvent, 1, 2500.0);
        saveBooking(organizerBEvent, 9, 22500.0);
    }

    @Test
    @WithMockUser(username = ORGANIZER_A_EMAIL, authorities = "ROLE_EVENT_ORGANIZER")
    void returnsOnlyAuthenticatedOrganizersEventsAndAggregates() throws Exception {
        mockMvc.perform(get("/bookings/organizer/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].eventId").value(organizerAEvent.getId()))
                .andExpect(jsonPath("$[0].eventName").value("Organizer A event"))
                .andExpect(jsonPath("$[0].totalTickets").value(3))
                .andExpect(jsonPath("$[0].totalRevenue").value(7500.0))
                .andExpect(jsonPath("$[0].bookingCount").value(2))
                .andExpect(jsonPath("$[1].eventId").value(organizerAEventWithoutBookings.getId()))
                .andExpect(jsonPath("$[1].totalTickets").value(0))
                .andExpect(jsonPath("$[1].totalRevenue").value(0.0))
                .andExpect(jsonPath("$[1].bookingCount").value(0));
    }

    @Test
    @WithMockUser(username = ORGANIZER_A_EMAIL, authorities = "ROLE_EVENT_ORGANIZER")
    void organizerEventListingReturnsOnlyAuthenticatedOrganizersEvents() throws Exception {
        mockMvc.perform(get("/events/organizer/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(organizerAEvent.getId()))
                .andExpect(jsonPath("$[1].id").value(organizerAEventWithoutBookings.getId()));
    }

    @Test
    @WithMockUser(username = "customer-stats@example.test", authorities = "ROLE_CUSTOMER")
    void deniesCustomersAccessToOrganizerStatistics() throws Exception {
        mockMvc.perform(get("/bookings/organizer/statistics"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer-stats@example.test", authorities = "ROLE_CUSTOMER")
    void deniesCustomersAccessToOrganizerEventListing() throws Exception {
        mockMvc.perform(get("/events/organizer/mine"))
                .andExpect(status().isForbidden());
    }

        @Test
        @WithMockUser(username = ORGANIZER_A_EMAIL, authorities = "ROLE_EVENT_ORGANIZER")
        void newlyCreatedEventIsOwnedByAuthenticatedOrganizer() throws Exception {
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "eventName": "New organizer event",
                      "eventDate": "2026-10-12",
                      "ticketPrice": 2500,
                      "contactNumber": "1234567890",
                        "organizerName": "Organizer A",
                        "eventDescription": "An evening of live music and local artists."
                    }
                    """))
            .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eventName").value("New organizer event"))
                    .andExpect(jsonPath("$.eventDescription").value("An evening of live music and local artists."));

        mockMvc.perform(get("/bookings/organizer/statistics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[2].eventName").value("New organizer event"));
        }

        @Test
        @WithMockUser(username = ORGANIZER_A_EMAIL, authorities = "ROLE_EVENT_ORGANIZER")
        void eventDescriptionCanBeUpdatedAndRead() throws Exception {
        mockMvc.perform(put("/events/{id}", organizerAEvent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "eventName": "Organizer A event",
                      "eventDate": "2026-10-12",
                      "ticketPrice": 2500,
                      "contactNumber": "1234567890",
                      "organizerName": "Organizer A",
                      "eventDescription": "Updated event information and highlights."
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(content().string("Event updated successfully"));

        mockMvc.perform(get("/events/{id}", organizerAEvent.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.eventDescription").value("Updated event information and highlights."));
        }

        @Test
        @WithMockUser(username = "customer-stats@example.test", authorities = "ROLE_CUSTOMER")
        void customerBookingStillPersistsNormally() throws Exception {
        long bookingCountBefore = bookingRepository.count();

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "eventId": %d,
                      "numberOfTickets": 4
                    }
                    """.formatted(organizerAEvent.getId())))
            .andExpect(status().isOk())
            .andExpect(content().string("Booking successful"));

        bookingRepository.flush();
        org.junit.jupiter.api.Assertions.assertEquals(bookingCountBefore + 1, bookingRepository.count());
        }

        @Test
        void publicEventBrowseStillWorks() throws Exception {
                mockMvc.perform(get("/events"))
                                .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].eventName", hasItem("Organizer A event")));
        }

        @Test
        void customerCanRegisterAndLoginUsingExistingAuthEndpoints() throws Exception {
                mockMvc.perform(post("/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "name": "Statistics Regression Customer",
                                                                    "email": "statistics-regression-customer@example.test",
                                                                    "password": "TestPass123!",
                                                                    "role": "CUSTOMER"
                                                                }
                                                                """))
                                .andExpect(status().isOk())
                                .andExpect(content().string("User registered successfully"));

                String token = mockMvc.perform(post("/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "email": "statistics-regression-customer@example.test",
                                                                    "password": "TestPass123!"
                                                                }
                                                                """))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                assertEquals(3, token.split("\\.").length);
        }

    private User createUser(String email, String role) {
        User user = new User();
        user.setName(email);
        user.setEmail(email);
        user.setPassword("not-used-by-this-test");
        user.setRole(role);
        return user;
    }

    private Event createEvent(String eventName, User organizer) {
        Event event = new Event();
        event.setEventName(eventName);
        event.setTicketPrice(2500.0);
        event.setOrganizerName(organizer.getName());
        event.setOrganizer(organizer);
        return event;
    }

    private void saveBooking(Event event, int ticketCount, double totalAmount) {
        Booking booking = new Booking();
        booking.setEventId(event.getId());
        booking.setNumberOfTickets(ticketCount);
        booking.setTotalAmount(totalAmount);
        booking.setBookingStatus("CONFIRMED");
        bookingRepository.save(booking);
    }
}
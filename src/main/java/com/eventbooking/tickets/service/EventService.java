package com.eventbooking.tickets.service;

import com.eventbooking.tickets.dto.EventCreatedto;
import com.eventbooking.tickets.entity.Event;
import com.eventbooking.tickets.entity.User;
import com.eventbooking.tickets.repository.EventRepository;
import com.eventbooking.tickets.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }
    //create the event
    public Event createEvent(EventCreatedto eventCreateDTO) {

        Event event = new Event();

        event.setEventName(eventCreateDTO.getEventName());
        event.setTicketPrice(eventCreateDTO.getTicketPrice());
        event.setContactNumber(eventCreateDTO.getContactNumber());
        event.setOrganizerName(eventCreateDTO.getOrganizerName());
        event.setEventDate(eventCreateDTO.getEventDate());
        event.setEventDescription(eventCreateDTO.getEventDescription());
        String organizerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User organizer = userRepository.findByEmail(organizerEmail)
            .orElseThrow(() -> new IllegalStateException("Authenticated organizer was not found."));
        event.setOrganizer(organizer);


        return eventRepository.save(event);
    }
    //get the event details
    public List<Event> getEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getOrganizerEvents(String authenticatedEmail) {
        User organizer = userRepository.findByEmail(authenticatedEmail)
                .filter(user -> "EVENT_ORGANIZER".equals(user.getRole()))
                .orElseThrow(() -> new AccessDeniedException("An event organizer account is required."));

        return eventRepository.findByOrganizer_Id(organizer.getId());
    }

    //GET BY EVENTID
    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElse(null);
    }
    //edit by id
    public String updateEvent(Long id, EventCreatedto eventCreatedto) {

        Event event = eventRepository.findById(id).orElse(null);

        if (event == null) {
            return "Event not found";
        }

        event.setEventName(eventCreatedto.getEventName());
        event.setTicketPrice(eventCreatedto.getTicketPrice());
        event.setContactNumber(eventCreatedto.getContactNumber());
        event.setOrganizerName(eventCreatedto.getOrganizerName());
        event.setEventDate(eventCreatedto.getEventDate());
        if (eventCreatedto.getEventDescription() != null) {
            event.setEventDescription(eventCreatedto.getEventDescription());
        }
        eventRepository.save(event);

        return "Event updated successfully";
    }
    //delete by id
    public String deleteEvent(Long id) {

        Event event = eventRepository.findById(id).orElse(null);

        if (event == null) {
            return "Event not found";
        }

        eventRepository.deleteById(id);

        return "Event deleted successfully";
    }
}
package com.eventbooking.tickets.controller;

import java.util.List;
import com.eventbooking.tickets.dto.EventCreatedto;
import com.eventbooking.tickets.entity.Event;
import com.eventbooking.tickets.service.EventService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public Event createEvent(@RequestBody EventCreatedto eventCreateDTO) {
        return eventService.createEvent(eventCreateDTO);
    }

    @GetMapping
    public List<Event> getEvents() {
        return eventService.getEvents();
    }

    @GetMapping("/organizer/mine")
    public List<Event> getOrganizerEvents(Authentication authentication) {
        return eventService.getOrganizerEvents(authentication.getName());
    }

    @GetMapping("/{id}")
    public Event getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }
    @PutMapping("/{id}")
    public String updateEvent(
            @PathVariable Long id,
            @RequestBody EventCreatedto eventCreatedto) {

        return eventService.updateEvent(id, eventCreatedto);
    }
    @DeleteMapping("/{id}")
    public String deleteEvent(@PathVariable Long id) {
        return eventService.deleteEvent(id);
    }
}
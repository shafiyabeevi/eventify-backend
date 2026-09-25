package com.eventbooking.tickets.repository;

import com.eventbooking.tickets.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

	List<Event> findByOrganizer_Id(Long organizerId);

}
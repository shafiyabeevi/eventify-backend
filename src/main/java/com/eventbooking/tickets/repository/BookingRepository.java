package com.eventbooking.tickets.repository;

import com.eventbooking.tickets.dto.OrganizerEventStatisticsDto;
import com.eventbooking.tickets.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

	@Query("""
			select new com.eventbooking.tickets.dto.OrganizerEventStatisticsDto(
				eventRecord.id,
				eventRecord.eventName,
				sum(bookingRecord.numberOfTickets),
				sum(bookingRecord.totalAmount),
				count(bookingRecord.id)
			)
			from Event eventRecord
			left join Booking bookingRecord on bookingRecord.eventId = eventRecord.id
			where eventRecord.organizer.id = :organizerId
			group by eventRecord.id, eventRecord.eventName
			order by eventRecord.id
			""")
	List<OrganizerEventStatisticsDto> findStatisticsForOrganizer(@Param("organizerId") Long organizerId);

}
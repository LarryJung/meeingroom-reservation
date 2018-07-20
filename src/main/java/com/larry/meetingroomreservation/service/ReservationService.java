package com.larry.meetingroomreservation.service;

import com.larry.meetingroomreservation.domain.entity.Reservation;
import com.larry.meetingroomreservation.domain.entity.Room;
import com.larry.meetingroomreservation.domain.entity.User;
import com.larry.meetingroomreservation.domain.entity.dto.ReservationRequestDto;
import com.larry.meetingroomreservation.domain.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private final Logger log = LoggerFactory.getLogger(ReservationService.class);

    @Autowired
    private ReservationRepository reservationRepository;

    public List<Reservation> findAllByDateAndRoomId(String reservedDate, Long roomId) {
        LocalDate localDate = LocalDate.parse(reservedDate);
        log.info("local date : {}", localDate);
        return reservationRepository.findAllByMeetingTimeReservedDateAndReservedRoomId(localDate, roomId);
    }

    public Reservation reserve(User loginUser, ReservationRequestDto target, Room room) {
        Reservation reservation = target.toEntity();
        reservation.bookBy(loginUser).assignRoom(room);
        if (!isReservable(reservation)) {
            throw new RuntimeException("예약할 수 없습니다.");
        }
        return reservationRepository.save(reservation);
    }

    private boolean isReservable(Reservation reservation) {
        List<Reservation> reservations = reservationRepository.findAllByMeetingTimeReservedDateAndReservedRoom(
                reservation.getReservedDate(), reservation.getReservedRoom());
        if (reservations.isEmpty()) {
            return true;
        }
        return reservations.stream().noneMatch(r -> r.isOverlap(reservation));
    }

}

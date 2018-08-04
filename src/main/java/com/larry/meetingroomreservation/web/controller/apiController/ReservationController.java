package com.larry.meetingroomreservation.web.controller.apiController;

import com.larry.meetingroomreservation.domain.entity.Reservation;
import com.larry.meetingroomreservation.domain.entity.Room;
import com.larry.meetingroomreservation.domain.entity.User;
import com.larry.meetingroomreservation.domain.entity.dto.ReservationRequestDto;
import com.larry.meetingroomreservation.service.ReservationService;
import com.larry.meetingroomreservation.service.RoomService;
import com.larry.meetingroomreservation.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RequestMapping("/api/reservations")
@RestController
public class ReservationController {

    private final Logger log = LoggerFactory.getLogger(ReservationController.class);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @GetMapping("/{reservedDate}/rooms/{roomId}")
    public ResponseEntity<List<Reservation>> retrieveReservations(@PathVariable String reservedDate, @PathVariable Long roomId) {
        List<Reservation> reservations = reservationService.findAllByDateAndRoomId(reservedDate, roomId);
        log.info("found reservations : {}", reservations);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(reservations);
    }

    @PreAuthorize("hasAuthority('SCOPE_USER')")
    @PostMapping("/{reservedDate}/rooms/{roomId}")
    public ResponseEntity<Reservation> registerReservation(@AuthenticationPrincipal Long id, @RequestBody @Valid ReservationRequestDto reservationDto, @PathVariable String reservedDate, @PathVariable Long roomId) {
        log.info("principal (id) : {}", id);
        User loginUser = userService.findById(id);
        Room room = roomService.findById(roomId);
        log.info("reserved dto : {}", reservationDto);
        Reservation reservation = reservationService.reserve(loginUser, reservationDto, room);
        URI url = URI.create(String.format("/api/reservations/%s/rooms/%d", reservedDate, reservation.getId()));
        return ResponseEntity.created(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(reservation);
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or (#id == #bookerId)")
    @DeleteMapping("/{reservedDate}/rooms/{roomId}")
    public ResponseEntity<Void> cancelReservation(@AuthenticationPrincipal Long id, @RequestParam String reservationId, @RequestParam("bookerId") Long bookerId) {
        log.info("reservation id : {}", reservationId);
        reservationService.deleteById(Long.valueOf(reservationId));
        return ResponseEntity.ok().build();
    }

}

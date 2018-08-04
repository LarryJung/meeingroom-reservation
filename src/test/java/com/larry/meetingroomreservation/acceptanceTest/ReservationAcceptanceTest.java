package com.larry.meetingroomreservation.acceptanceTest;

import com.larry.meetingroomreservation.acceptanceTest.support.AbstractAcceptanceTest;
import com.larry.meetingroomreservation.domain.entity.*;
import com.larry.meetingroomreservation.domain.entity.dto.ReservationRequestDto;
import com.larry.meetingroomreservation.domain.validation.ErrorMsg;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ReservationAcceptanceTest extends AbstractAcceptanceTest{

    private final Logger log = LoggerFactory.getLogger(ReservationAcceptanceTest.class);

    @Test
    public void getReservationListByDateAndRoomName() throws IOException {
        Long reservedRoomId = 1L;
        LocalDate date = LocalDate.of(2018, 8, 1);
        String url = String.format("/api/reservations/%s/rooms/%d", toStringDateTime(date), reservedRoomId);
        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        List<Reservation> reservations = convertList(response, Reservation.class);
        Reservation reservation = reservations.get(0);
        assertThat(reservations.size(), is(2));
        assertThat(reservation, instanceOf(Reservation.class));
        assertThat(reservation.getMeetingTime().getReservedDate(), is(LocalDate.of(2018, 8, 1)));
    }

    @Test
    public void registerReservation_login() {
        LocalDate reservedDate = LocalDate.of(2018, 8, 3);
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(reservedDate, LocalTime.of(10, 0), LocalTime.of(11, 0), 4);
        HttpEntity<ReservationRequestDto> entity = makeJwtEntity(KAKAO_정채균, reservationRequestDto);
        ResponseEntity<Reservation> response = restTemplate.postForEntity(String.format("/api/reservations/%s/rooms/%d", reservedDate, 1), entity, Reservation.class);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(response.getBody().getReservedDate(), is(reservedDate));
    }

    @Test(expected = RuntimeException.class)
    public void registerReservation_no_login() {
        LocalDate reservedDate = LocalDate.of(2018, 8, 4);
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(reservedDate, LocalTime.of(10, 0), LocalTime.of(11, 0), 4);
        ResponseEntity<Reservation> response = restTemplate.postForEntity(String.format("/api/reservations/%s/rooms/%d", reservedDate, 1), reservationRequestDto, Reservation.class);
    }

    @Test
    public void registerReservation_wrong_params() {
        LocalDate reservedDate = LocalDate.of(2018, 8, 5);
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(reservedDate, LocalTime.of(11, 0), LocalTime.of(10, 0), 4);
        HttpEntity<ReservationRequestDto> entity = makeJwtEntity(KAKAO_정채균, reservationRequestDto);
        ErrorMsg response = restTemplate.postForObject(String.format("/api/reservations/%s/rooms/%d", reservedDate, 1), entity, ErrorMsg.class);
        assertThat(response.getMessage(), is("시작시간은 끝나는 시간 이전이어야 합니다."));
    }

    @Test
    public void deleteReservation_owner() {
        LocalDate reservedDate = LocalDate.of(2018, 8, 6);
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(reservedDate, LocalTime.of(10, 0), LocalTime.of(11, 0), 4);
        String reservationId = createReservation(reservationRequestDto);
        HttpEntity<ReservationRequestDto> deleteEntity = makeJwtEntity(KAKAO_정채균);
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(String.format("/api/reservations/%s/rooms/%d?reservationId=%s&bookerId=3", reservedDate, 1, reservationId), HttpMethod.DELETE, deleteEntity, Void.class);
        assertThat(deleteResponse.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void deleteReservation_admin() {
        LocalDate reservedDate = LocalDate.of(2018, 8, 7);
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(reservedDate, LocalTime.of(10, 0), LocalTime.of(11, 0), 4);
        String reservationId = createReservation(reservationRequestDto);
        HttpEntity<ReservationRequestDto> deleteEntity = makeJwtEntity(Larry);
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(String.format("/api/reservations/%s/rooms/%d?reservationId=%s&bookerId=3", reservedDate, 1, reservationId), HttpMethod.DELETE, deleteEntity, Void.class);
        assertThat(deleteResponse.getStatusCode(), is(HttpStatus.OK));
    }

    @Test(expected = RuntimeException.class)
    public void deleteReservation_other_role_user() {
        LocalDate reservedDate = LocalDate.of(2018, 8, 8);
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(reservedDate, LocalTime.of(10, 0), LocalTime.of(11, 0), 4);
        String reservationId = createReservation(reservationRequestDto);
        HttpEntity<ReservationRequestDto> deleteEntity = makeJwtEntity(Charry);
        restTemplate.exchange(String.format("/api/reservations/%s/rooms/%d?reservationId=%s&bookerId=3", reservedDate, 1, reservationId), HttpMethod.DELETE, deleteEntity, Void.class);
    }

    @Test(expected = RuntimeException.class)
    public void deleteReservation_no_login() {
        LocalDate reservedDate = LocalDate.of(2018, 8, 9);
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(reservedDate, LocalTime.of(10, 0), LocalTime.of(11, 0), 4);
        String reservationId = createReservation(reservationRequestDto);
        restTemplate.delete(String.format("/api/reservations/%s/rooms/%d?reservationId=%s", reservedDate, 1, reservationId));
    }

    public String createReservation(ReservationRequestDto reservationRequestDto) {
        HttpEntity<ReservationRequestDto> entity = makeJwtEntity(KAKAO_정채균, reservationRequestDto);
        ResponseEntity<Reservation> response = restTemplate.postForEntity(String.format("/api/reservations/%s/rooms/%d", reservationRequestDto.getReservedDate(), 1), entity, Reservation.class);
        return String.valueOf(response.getBody().getId());
    }


}

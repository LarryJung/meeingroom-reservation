package com.larry.meetingroomreservation.domain.entity;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.larry.meetingroomreservation.domain.entity.support.AbstractEntity;
import com.larry.meetingroomreservation.domain.exceptions.AlreadyReservedException;
import com.larry.meetingroomreservation.domain.exceptions.CannotReserveSameBookerPerDayException;
import com.larry.meetingroomreservation.domain.exceptions.ExcessAttendeeException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Reservation extends AbstractEntity{

    @JsonUnwrapped
    @Embedded
    private MeetingTime meetingTime;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn
    private Room reservedRoom;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn
    private User booker;

    @Min(3)
    @Column
    private Integer numberOfAttendee;

    public Reservation(MeetingTime meetingTime, Integer numberOfAttendee, Room room) {
        this.meetingTime = meetingTime;
        if (!room.isPossibleAttendeeNumber(numberOfAttendee)) {
            throw new ExcessAttendeeException(String.format("인원 초과입니다. 허용인원 : %d", room.getOccupancy()));
        }
        this.numberOfAttendee = numberOfAttendee;
        this.reservedRoom = room;
    }

    public Reservation(LocalDate reservedDate, LocalTime startTime, LocalTime endTime, Integer numberOfAttendee, Room room) {
        this(new MeetingTime(reservedDate, new Period(startTime, endTime)), numberOfAttendee, room);
    }

    public boolean isOverlap(Reservation target) {
        if (this.booker.equals(target.booker)) {
            throw new CannotReserveSameBookerPerDayException("예약은 해당 날짜에 1번만 가능합니다.");
        }
        if (this.meetingTime.isTimeOverlap(target.meetingTime)) {
            throw new AlreadyReservedException("겹치는 시간입니다.");
        }
        return false;
    }

    public Reservation bookBy(User loginUser) {
        this.booker = loginUser;
        return this;
    }

    public LocalDate getReservedDate() {
        return this.meetingTime.getReservedDate();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(meetingTime, that.meetingTime) &&
                Objects.equals(reservedRoom, that.reservedRoom) &&
                Objects.equals(booker, that.booker) &&
                Objects.equals(numberOfAttendee, that.numberOfAttendee);
    }

    @Override
    public int hashCode() {

        return Objects.hash(meetingTime, reservedRoom, booker, numberOfAttendee);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "meetingTime=" + meetingTime +
                ", reservedRoom=" + reservedRoom.getRoomName() +
                ", booker=" + booker.getUserId() +
                ", numberOfAttendee=" + numberOfAttendee +
                '}';
    }
}

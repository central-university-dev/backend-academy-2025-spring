package ru.tbank.sem12.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Statistics {
    private Integer totalHotels;
    private Integer totalRooms;
    private Integer totalRoomsAvailable;
    private Integer totalRoomsBooked;
    private Long averageRoomPrice;
}

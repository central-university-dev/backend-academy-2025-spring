package ru.tbank.sem12.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class RoomType {
    private String id;
    private String name;
    private List<String> amenities;
}

package ru.tbank.sem12.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Hotels {
    private List<Hotel> hotels;
}

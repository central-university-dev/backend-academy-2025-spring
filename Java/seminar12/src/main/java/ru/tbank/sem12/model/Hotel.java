package ru.tbank.sem12.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Hotel {
    private String id;
    private String name;
    private String address;
}

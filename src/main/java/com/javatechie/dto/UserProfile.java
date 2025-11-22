package com.javatechie.dto;

import java.util.List;

public record UserProfile(
        String name,
        int age,
        String country,
        List<String> skills,
        Address address
) {
    public record Address(String street, String city, String zip) {}
}

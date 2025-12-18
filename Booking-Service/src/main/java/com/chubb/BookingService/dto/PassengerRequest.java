package com.chubb.BookingService.dto;

import com.chubb.BookingService.enums.Gender;
import com.chubb.BookingService.enums.Meal_Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PassengerRequest {

    @NotBlank(message = "Passenger name cannot be empty")
    private String name;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Meal type is required")
    private Meal_Type mealType;
}



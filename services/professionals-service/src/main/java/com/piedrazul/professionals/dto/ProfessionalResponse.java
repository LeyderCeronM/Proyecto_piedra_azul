package com.piedrazul.professionals.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionalResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String documentNumber;
    private String specialty;
    private String phone;
    private String email;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

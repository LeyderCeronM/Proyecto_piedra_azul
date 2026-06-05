package com.piedrazul.professionals.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProfessionalRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Document number is required")
    @Size(max = 50)
    private String documentNumber;

    @Size(max = 100)
    private String specialty;

    @Size(max = 30)
    private String phone;

    @Email
    @Size(max = 150)
    private String email;
}

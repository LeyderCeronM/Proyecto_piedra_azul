package com.piedrazul.professionals.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfessionalRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 100)
    private String specialty;

    @Size(max = 30)
    private String phone;

    @Email
    @Size(max = 150)
    private String email;

    private Boolean active;
}

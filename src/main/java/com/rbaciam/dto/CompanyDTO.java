package com.rbaciam.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class    CompanyDTO {
	

    @NotBlank(message = "Name is mandatory")
    private String name;

    private String description;

    @NotBlank(message = "Domain is mandatory")
    @Pattern(
            regexp = "^(?:[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|[a-zA-Z0-9-]+\\.[a-zA-Z]{2,})$",
            message = "Must be a valid domain (example.com) or email (user@example.com)"
        )
    private String domain;

    private String country;

    private String address;

    private String timezone;

    private String industry;

    private String homepage;
    
    private LocalDateTime created_On; 
    
    @Schema(hidden = true)
    private int userCount;
    

}
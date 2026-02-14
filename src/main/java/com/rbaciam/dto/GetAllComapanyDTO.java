package com.rbaciam.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAllComapanyDTO {
		
        private Long id;    
	
	    @NotBlank(message = "Name is mandatory")
	    private String name;

	    private String description;

	    @NotBlank(message = "Domain is mandatory")
	    @Pattern(regexp = "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid domain format")
	    private String domain;

	    private String country;

	    private String address;

	    private String timezone;

	    private String industry;

	    private String homepage;
	    
	    private LocalDateTime created_On; 
	    
	    private int userCount;
	}


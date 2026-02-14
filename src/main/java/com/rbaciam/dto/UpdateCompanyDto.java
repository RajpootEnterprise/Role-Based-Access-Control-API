package com.rbaciam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompanyDto {
	    

	    @NotBlank(message = "Name is mandatory")
	    private String name;

	    private String description;

	    private String country;

	    private String address;
	    
	    private String timezone;
	    
	    private String industry;
	    
	    private String homepage;
	    
	
}

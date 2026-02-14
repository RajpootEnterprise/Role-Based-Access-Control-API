package com.rbaciam.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class AssignPasswordRequestDTO {
	
	@NotNull(message = "user Id is required")
	private Long userId;
	
	@NotNull(message = "password is required")
	@Size(min = 8, message = "Password must be at least 8 characters")
	private String password;

}

package com.rbaciam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "User creation request (Super Admin only)")
public class UserCreateDTO {

	@NotBlank(message = "Name is mandatory")
	private String name;

	@NotBlank(message = "Email is mandatory")
	@Email(message = "Invalid email format")
	private String email;


    @NotNull(message = "Role ID is mandatory")
    @Schema(example = "1", required = true)
    private Long roleId;

    @NotNull(message = "Company ID is mandatory")
    @Schema(example = "1", required = true)
    private Long companyId;
}


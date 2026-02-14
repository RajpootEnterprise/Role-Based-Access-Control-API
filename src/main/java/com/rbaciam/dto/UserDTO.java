package com.rbaciam.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class UserDTO {

	private Long userId;

	@NotBlank(message = "Name is mandatory")
	private String name;

	@NotBlank(message = "Email is mandatory")
	@Email(message = "Invalid email format")
	private String email;

	private Long role_Id;

	private Long companyId;

	private String status;

	private String companyName;

	private String roleName;

	private LocalDateTime created_On;
		   
	

	public record RoleChangeRequest(
			@Schema(description = "ID of the new role to assign", example = "2", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Role ID cannot be null") @Positive(message = "Role ID must be a positive number") Long roleId) {

		public static RoleChangeRequest of(Long roleId) {
			return new RoleChangeRequest(roleId);
		}
	}





}
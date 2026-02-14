package com.rbaciam.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoleDTO {
	

	    @NotBlank(message = "Name is mandatory")
	    private String name;

	    @NotBlank(message = "Type is mandatory")
	    private String type;

	    private List<PermissionDTO> permissions =new ArrayList<>();
	}


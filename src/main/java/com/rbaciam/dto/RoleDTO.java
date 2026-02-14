package com.rbaciam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import com.rbaciam.entity.Role;

@Getter
@Setter
public class RoleDTO {
     private Long id;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Type is mandatory")
    private String type;

    private List<PermissionDTO> permissions =new ArrayList<>();
}
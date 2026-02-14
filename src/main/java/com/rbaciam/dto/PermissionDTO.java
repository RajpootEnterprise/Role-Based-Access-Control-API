package com.rbaciam.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PermissionDTO {
	
	private Long id;
 
    @NotBlank
    private String name;
    
    private List<PermissionDTO> childPermissions = new ArrayList<>();
    
    private List<String> flattenPermissions(PermissionDTO permission) {
        List<String> result = new ArrayList<>();
        if (permission.getName() != null) {
            result.add(permission.getName());
        }
        if (permission.getChildPermissions() != null) {
            permission.getChildPermissions().stream()
                .flatMap(p -> flattenPermissions(p).stream())
                .forEach(result::add);
        }
        return result;
    }
}
package com.rbaciam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
	
    private String name;
    private String email;
    private Long roleId;
    private String status;
    private Long updatedBy_Id;
}

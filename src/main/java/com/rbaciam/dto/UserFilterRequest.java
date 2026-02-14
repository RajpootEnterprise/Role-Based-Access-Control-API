package com.rbaciam.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFilterRequest {
    private String email;
    private String status;
    private Long roleId;

}

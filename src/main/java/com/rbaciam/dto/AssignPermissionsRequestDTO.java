package com.rbaciam.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AssignPermissionsRequestDTO {

    @NotNull(message = "Role ID is required")
    private Long roleId;

    @NotEmpty(message = "At least one permission ID is required")
    private List<Long> permissionIds;
}
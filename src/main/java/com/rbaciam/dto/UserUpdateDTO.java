package com.rbaciam.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Schema(description = "User update request")
public class UserUpdateDTO {
    @Schema(example = "John Smith")
    private String name;

    @Schema(example = "1")
    @JsonProperty("role_id")
    private Long roleId;

    private String status;
    
    
}

package com.rbaciam.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IndustryResponse {
    private String name;

    public IndustryResponse(String name) {
        this.name = name;
    }

}

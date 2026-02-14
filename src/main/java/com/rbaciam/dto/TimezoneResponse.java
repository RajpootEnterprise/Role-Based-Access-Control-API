package com.rbaciam.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TimezoneResponse {
    private final String name;
    private final String utcOffset;
    private final String description;

}
package com.rbaciam.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class CountryResponse {
    private String name;
    private String officialName;
    private String region;


}

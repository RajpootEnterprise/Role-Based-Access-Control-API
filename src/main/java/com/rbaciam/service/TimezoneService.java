package com.rbaciam.service;

import java.util.List;

import com.rbaciam.dto.TimezoneResponse;

public interface TimezoneService {
    List<TimezoneResponse> getAllTimezones(Long userId);
}
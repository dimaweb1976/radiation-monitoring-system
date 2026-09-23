package com.example.demo;

import java.math.BigDecimal;

public record StationRequest(String name, String location, String ipAddress,
                             BigDecimal latitude, BigDecimal longitude) {}

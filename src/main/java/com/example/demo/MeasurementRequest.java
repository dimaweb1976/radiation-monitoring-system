package com.example.demo;

import java.math.BigDecimal;
import java.time.Instant;

public record MeasurementRequest(Long detectorId, BigDecimal value, String unit,
                                 String messageId, Instant measuredAt) {}

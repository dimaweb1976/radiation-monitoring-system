package com.example.demo;

import java.math.BigDecimal;

public record HeartbeatRequest(String messageId, BigDecimal cpuTemp,
                               BigDecimal freeDiskGb, BigDecimal memoryPercent) {}

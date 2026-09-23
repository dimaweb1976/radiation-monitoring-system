package com.example.demo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "station_heartbeat")
public class StationHeartbeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    private String status;

    @Column(name = "message_id", unique = true)
    private String messageId;

    @Column(name = "cpu_temp")
    private BigDecimal cpuTemp;

    @Column(name = "free_disk_gb")
    private BigDecimal freeDiskGb;

    @Column(name = "memory_percent")
    private BigDecimal memoryPercent;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Station getStation() { return station; }
    public String getStatus() { return status; }
    public BigDecimal getCpuTemp() { return cpuTemp; }
    public BigDecimal getFreeDiskGb() { return freeDiskGb; }
    public BigDecimal getMemoryPercent() { return memoryPercent; }
    public Instant getCreatedAt() { return createdAt; }

    public void setStation(Station station) { this.station = station; }
    public void setStatus(String status) { this.status = status; }
    public void setCpuTemp(BigDecimal cpuTemp) { this.cpuTemp = cpuTemp; }
    public void setFreeDiskGb(BigDecimal freeDiskGb) { this.freeDiskGb = freeDiskGb; }
    public void setMemoryPercent(BigDecimal memoryPercent) { this.memoryPercent = memoryPercent; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
}

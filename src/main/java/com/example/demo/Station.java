package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
@Entity
@Table(name = "stations")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;

    @Column(name = "ip_address")
    private String ipAddress;

    private String status;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;
    private BigDecimal latitude;
    private BigDecimal longitude;
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getIpAddress() { return ipAddress; }
    public String getStatus() { return status; }
    public LocalDateTime getLastSeen() { return lastSeen; }

    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setStatus(String status) { this.status = status; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
}

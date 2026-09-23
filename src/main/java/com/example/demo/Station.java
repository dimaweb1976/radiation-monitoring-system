package com.example.demo;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.Duration;
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

    @Column(name = "last_seen")
    private Instant lastSeen;
    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;
    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getIpAddress() { return ipAddress; }
    public String getStatus() {
        if (lastSeen == null) return "OFFLINE";
        long seconds = Duration.between(lastSeen, Instant.now()).getSeconds();
        if (seconds > 300) return "OFFLINE";
        if (seconds > 60) return "WARNING";
        return "ONLINE";
    }
    public Instant getLastSeen() { return lastSeen; }

    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
}

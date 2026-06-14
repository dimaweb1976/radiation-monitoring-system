package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "detectors")
public class Detector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private String type;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getType() { return type; }
    public Station getStation() { return station; }

    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setType(String type) { this.type = type; }
    public void setStation(Station station) { this.station = station; }
}

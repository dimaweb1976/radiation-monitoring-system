package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StationEventRepository extends JpaRepository<StationEvent, Long> {

    List<StationEvent> findTop50ByOrderByCreatedAtDesc();
}

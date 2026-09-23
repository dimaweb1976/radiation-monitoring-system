package com.example.demo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

// Retain the existing timestamp columns; new values are stored as UTC.
@Converter(autoApply = true)
public class UtcInstantConverter implements AttributeConverter<Instant, LocalDateTime> {
    @Override
    public LocalDateTime convertToDatabaseColumn(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    @Override
    public Instant convertToEntityAttribute(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}

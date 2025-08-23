package tmd.tmdAdmin.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * @author : yahyai
 * @mailto : yibrahim.py@gmail.com
 **/
@Converter(autoApply = true)
public class LocalDateTimeToLongConverter implements AttributeConverter<LocalDateTime, Long> {

    @Override
    public Long convertToDatabaseColumn(LocalDateTime attribute) {
        if (attribute == null) {
            return null;
        }
        // Convert LocalDateTime to epoch milliseconds (UTC)
        return attribute.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Long dbData) {
        if (dbData == null) {
            return null;
        }
        // Convert epoch milliseconds to LocalDateTime (UTC)
        return Instant.ofEpochMilli(dbData).atZone(ZoneId.of("UTC")).toLocalDateTime();
    }
}

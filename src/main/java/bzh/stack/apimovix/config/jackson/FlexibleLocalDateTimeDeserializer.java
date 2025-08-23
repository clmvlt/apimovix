package bzh.stack.apimovix.config.jackson;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Désérialiseur LocalDateTime acceptant plusieurs formats:
 * - yyyy-MM-dd
 * - yyyy-MM-dd HH:mm
 * - yyyy-MM-dd HH:mm:ss
 */
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = new ArrayList<>();
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Paris");

    static {
        DATE_TIME_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        DATE_TIME_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        DATE_TIME_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        DATE_TIME_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        String text = parser.getText();
        if (text == null) {
            return null;
        }
        String value = text.trim();
        if (value.isEmpty()) {
            return null;
        }

        // Essai ISO avec offset (ex: 2025-02-08T08:56:04.7209151+02:00 ou Z)
        try {
            if (value.contains("T") && (value.contains("+") || value.endsWith("Z") || value.contains("-"))) {
                OffsetDateTime odt = OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                return odt.atZoneSameInstant(DEFAULT_ZONE).toLocalDateTime();
            }
        } catch (DateTimeParseException ignored) {
            // continue
        }

        // Essai format date seule
        try {
            if (value.length() == 10) {
                LocalDate date = LocalDate.parse(value, FORMAT_DATE);
                return date.atStartOfDay();
            }
        } catch (DateTimeParseException ignored) {
            // continue
        }

        // Essai formats datetime
        for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }

        // Laisser Jackson générer une erreur standard
        throw ctxt.weirdStringException(value, LocalDateTime.class, "Format de date/heure non supporté. Utilisez 'yyyy-MM-dd', 'yyyy-MM-dd HH:mm' ou 'yyyy-MM-dd HH:mm:ss'.");
    }
}



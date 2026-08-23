package in.setu.catalog.ai;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum IndianState {
    WEST_BENGAL;

    @JsonCreator
    public static IndianState from(String value) {
        if (value == null) return null;
        return switch (value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_')) {
            case "WEST_BENGAL", "WB", "BENGAL" -> WEST_BENGAL;
            default -> throw new IllegalArgumentException("Unsupported or ambiguous state: " + value);
        };
    }
}

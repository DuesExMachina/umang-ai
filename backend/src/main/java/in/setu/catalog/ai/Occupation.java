package in.setu.catalog.ai;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum Occupation {
    STUDENT, UNEMPLOYED, EMPLOYED, SELF_EMPLOYED, FARMER;

    @JsonCreator
    public static Occupation from(String value) {
        if (value == null) return null;
        return switch (value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_')) {
            case "STUDENT", "STUDYING", "STUDIES", "LEARNER" -> STUDENT;
            case "UNEMPLOYED", "JOBLESS", "LOOKING_FOR_WORK" -> UNEMPLOYED;
            case "EMPLOYED", "WORKING", "SALARIED" -> EMPLOYED;
            case "SELF_EMPLOYED", "SELFEMPLOYED", "BUSINESS_OWNER" -> SELF_EMPLOYED;
            case "FARMER", "AGRICULTURIST" -> FARMER;
            default -> throw new IllegalArgumentException("Unsupported or ambiguous occupation: " + value);
        };
    }
}

package in.setu.catalog.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/** Nullable, extracted facts only. Absence means the user did not clearly state the fact. */
public record UserProfilePatch(
    @JsonProperty(required = false) Integer age,
    @JsonProperty(required = false) IndianState state,
    @JsonProperty(required = false) Occupation occupation,
    @JsonProperty(required = false) BigDecimal annualFamilyIncome
) {
    public boolean isEmpty() { return age == null && state == null && occupation == null && annualFamilyIncome == null; }
}

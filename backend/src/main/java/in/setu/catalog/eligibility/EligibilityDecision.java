package in.setu.catalog.eligibility;

import java.util.List;

public record EligibilityDecision(EligibilityStatus status, List<String> missingAttributes, List<String> unmetRequirements) {
    public EligibilityDecision { missingAttributes = List.copyOf(missingAttributes); unmetRequirements = List.copyOf(unmetRequirements); }
}

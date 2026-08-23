package in.setu.catalog.eligibility;

/** A deterministic evaluation outcome; it is never a final authority decision. */
public enum EligibilityStatus {
    MATCHED, POTENTIALLY_MATCHED, NOT_MATCHED, MISSING_INFORMATION
}

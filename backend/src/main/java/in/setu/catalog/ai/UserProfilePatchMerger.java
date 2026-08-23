package in.setu.catalog.ai;

import in.setu.catalog.eligibility.UserProfile;

/** Applies only non-null, validated facts. Existing profile facts are never cleared by extraction. */
public final class UserProfilePatchMerger {
    public UserProfile merge(UserProfile currentProfile, UserProfilePatch patch) {
        UserProfile.Builder merged = UserProfile.builder();
        currentProfile.attributes().forEach(merged::attribute);
        if (patch.age() != null) merged.attribute("age", patch.age());
        if (patch.state() != null) merged.attribute("state", patch.state().name());
        if (patch.occupation() != null) merged.attribute("occupation", patch.occupation().name());
        // Canonical profile field used by the existing eligibility rule DSL.
        if (patch.annualFamilyIncome() != null) merged.attribute("annualHouseholdIncome", patch.annualFamilyIncome());
        return merged.build();
    }
}

package in.setu.catalog.ai;

/** Boundary for natural-language extraction. It may extract facts, but never evaluates scheme eligibility. */
public interface ProfileExtractionPort {
    UserProfilePatch extract(String userMessage);
}

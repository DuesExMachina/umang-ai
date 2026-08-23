package in.setu.catalog.ai;

import in.setu.catalog.eligibility.UserProfile;

/** Coordinates extraction, validation, and safe profile merge; it never invokes the eligibility engine. */
public final class ProfileExtractionService {
    private final ProfileExtractionPort extractor;
    private final UserProfilePatchValidator validator;
    private final UserProfilePatchMerger merger;

    public ProfileExtractionService(ProfileExtractionPort extractor) {
        this(extractor, new UserProfilePatchValidator(), new UserProfilePatchMerger());
    }

    ProfileExtractionService(ProfileExtractionPort extractor, UserProfilePatchValidator validator, UserProfilePatchMerger merger) {
        this.extractor = extractor; this.validator = validator; this.merger = merger;
    }

    public UserProfilePatch extractValidatedPatch(String message) { return validator.validate(extractor.extract(message)); }
    public UserProfile mergeInto(UserProfile currentProfile, String message) { return merger.merge(currentProfile, extractValidatedPatch(message)); }
}

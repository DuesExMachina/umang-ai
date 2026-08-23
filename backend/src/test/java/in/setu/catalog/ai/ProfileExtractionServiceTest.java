package in.setu.catalog.ai;

import static org.assertj.core.api.Assertions.assertThat;

import in.setu.catalog.eligibility.UserProfile;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProfileExtractionServiceTest {
    @Test
    void extractsAndMergesCompleteProfileInformation() {
        ProfileExtractionService service = serviceFor(new UserProfilePatch(24, IndianState.from("WB"), Occupation.from("studying"), new BigDecimal("250000")));

        UserProfile profile = service.mergeInto(UserProfile.builder().build(), "I'm 24, a student from West Bengal and my family income is around 2.5 lakh per year.");

        assertThat(profile.attribute("age")).contains(24);
        assertThat(profile.attribute("state")).contains("WEST_BENGAL");
        assertThat(profile.attribute("occupation")).contains("STUDENT");
        assertThat(profile.attribute("annualHouseholdIncome")).contains(new BigDecimal("250000"));
    }

    @Test
    void appliesOnlyPartialInformation() {
        ProfileExtractionService service = serviceFor(new UserProfilePatch(null, null, Occupation.STUDENT, null));

        UserProfile profile = service.mergeInto(UserProfile.builder().attribute("age", 30).build(), "I am studying.");

        assertThat(profile.attribute("occupation")).contains("STUDENT");
        assertThat(profile.attribute("age")).contains(30);
        assertThat(profile.hasAttribute("state")).isFalse();
    }

    @Test
    void leavesProfileUntouchedWhenNothingCanBeExtracted() {
        ProfileExtractionService service = serviceFor(new UserProfilePatch(null, null, null, null));
        UserProfile original = UserProfile.builder().attribute("age", 42).build();

        UserProfile profile = service.mergeInto(original, "Hello there");

        assertThat(profile.attributes()).containsExactlyEntriesOf(original.attributes());
    }

    @Test
    void leavesAmbiguousFieldsUnsetAndNeverClearsKnownValue() {
        ProfileExtractionService service = serviceFor(new UserProfilePatch(null, null, null, null));

        UserProfile profile = service.mergeInto(UserProfile.builder().attribute("state", "WEST_BENGAL").build(), "I may be from Bengal or Bihar.");

        assertThat(profile.attribute("state")).contains("WEST_BENGAL");
    }

    @Test
    void dropsInvalidAgeBeforeMerge() {
        ProfileExtractionService service = serviceFor(new UserProfilePatch(121, null, null, null));

        UserProfile profile = service.mergeInto(UserProfile.builder().attribute("age", 24).build(), "I am 121.");

        assertThat(profile.attribute("age")).contains(24);
    }

    @Test
    void dropsInvalidIncomeBeforeMerge() {
        ProfileExtractionService service = serviceFor(new UserProfilePatch(null, null, null, new BigDecimal("-1")));

        UserProfile profile = service.mergeInto(UserProfile.builder().build(), "My income is minus one.");

        assertThat(profile.hasAttribute("annualHouseholdIncome")).isFalse();
    }

    @Test
    void normalizesNaturalLanguageIncomeAndCommonVariantsThroughTypedPatch() {
        ProfileExtractionService service = serviceFor(new UserProfilePatch(24, IndianState.from("Bengal"), Occupation.from("student"), new BigDecimal("250000")));

        UserProfilePatch patch = service.extractValidatedPatch("I'm 24, a student from West Bengal and my family income is around 2.5 lakh per year.");

        assertThat(patch).isEqualTo(new UserProfilePatch(24, IndianState.WEST_BENGAL, Occupation.STUDENT, new BigDecimal("250000")));
    }

    private ProfileExtractionService serviceFor(UserProfilePatch result) {
        return new ProfileExtractionService(message -> result);
    }
}

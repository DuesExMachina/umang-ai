package in.setu.catalog.eligibility;

import static org.assertj.core.api.Assertions.assertThat;

import in.setu.catalog.domain.EligibilityRule;
import in.setu.catalog.domain.PublicationStatus;
import in.setu.catalog.domain.Scheme;
import in.setu.catalog.domain.SchemeCategory;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EligibilityEngineTest {
    private final EligibilityEngine engine = new EligibilityEngine();

    @Test
    void matchesInclusiveBoundaryValues() {
        Scheme scheme = scheme("DEMO-BOUNDARY", "{\"all\":[{\"field\":\"age\",\"between\":[18,60]},{\"field\":\"annualHouseholdIncome\",\"lte\":250000}]}");

        var decision = engine.evaluate(UserProfile.builder().attribute("age", 18).attribute("annualHouseholdIncome", 250000).build(), scheme);

        assertThat(decision.status()).isEqualTo(EligibilityStatus.MATCHED);
    }

    @Test
    void doesNotMatchValuesOutsideBoundary() {
        Scheme scheme = scheme("DEMO-BOUNDARY", "{\"field\":\"age\",\"gte\":18}");

        var decision = engine.evaluate(UserProfile.builder().attribute("age", 17).build(), scheme);

        assertThat(decision.status()).isEqualTo(EligibilityStatus.NOT_MATCHED);
        assertThat(decision.unmetRequirements()).containsExactly("age");
    }

    @Test
    void reportsMissingInformationWhenNoRequiredFactIsKnown() {
        Scheme scheme = scheme("DEMO-MISSING", "{\"all\":[{\"field\":\"age\",\"gte\":18},{\"field\":\"residentOfIndia\",\"booleanIs\":true}]}");

        var decision = engine.evaluate(UserProfile.builder().build(), scheme);

        assertThat(decision.status()).isEqualTo(EligibilityStatus.MISSING_INFORMATION);
        assertThat(decision.missingAttributes()).containsExactlyInAnyOrder("age", "residentOfIndia");
    }

    @Test
    void reportsPotentialMatchWhenKnownFactsMatchButAnotherFactIsMissing() {
        Scheme scheme = scheme("DEMO-POTENTIAL", "{\"all\":[{\"field\":\"student\",\"booleanIs\":true},{\"field\":\"annualHouseholdIncome\",\"lte\":250000}]}");

        var decision = engine.evaluate(UserProfile.builder().attribute("student", true).build(), scheme);

        assertThat(decision.status()).isEqualTo(EligibilityStatus.POTENTIALLY_MATCHED);
        assertThat(decision.missingAttributes()).containsExactly("annualHouseholdIncome");
    }

    @Test
    void rejectsConflictingRulesWhenOneRuleCannotBeSatisfied() {
        Scheme scheme = scheme("DEMO-CONFLICT", "{\"field\":\"residentOfIndia\",\"booleanIs\":true}", "{\"field\":\"residentOfIndia\",\"booleanIs\":false}");

        var decision = engine.evaluate(UserProfile.builder().attribute("residentOfIndia", true).build(), scheme);

        assertThat(decision.status()).isEqualTo(EligibilityStatus.NOT_MATCHED);
    }

    @Test
    void evaluatesMultipleSchemesIndependently() {
        Scheme studentScheme = scheme("DEMO-STUDENT", "{\"field\":\"student\",\"booleanIs\":true}");
        Scheme farmerScheme = scheme("DEMO-FARMER", "{\"field\":\"farmer\",\"booleanIs\":true}");
        UserProfile profile = UserProfile.builder().attribute("student", true).attribute("farmer", true).build();

        var decisions = engine.evaluate(profile, List.of(studentScheme, farmerScheme));

        assertThat(decisions).hasSize(2);
        assertThat(decisions.get("DEMO-STUDENT").status()).isEqualTo(EligibilityStatus.MATCHED);
        assertThat(decisions.get("DEMO-FARMER").status()).isEqualTo(EligibilityStatus.MATCHED);
    }

    private Scheme scheme(String code, String... rules) {
        Scheme scheme = new Scheme(UUID.randomUUID(), code, code, "Synthetic test scheme.", SchemeCategory.EDUCATION, true,
            "Synthetic test scheme only.", PublicationStatus.PUBLISHED, 1, "test-1");
        for (String rule : rules) scheme.addEligibilityRule(new EligibilityRule(UUID.randomUUID(), rule, LocalDate.of(2026, 1, 1), null));
        return scheme;
    }
}

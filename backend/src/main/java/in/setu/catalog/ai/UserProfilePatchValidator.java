package in.setu.catalog.ai;

import java.math.BigDecimal;

/** Rejects implausible or unsafe LLM values before any profile merge. */
public final class UserProfilePatchValidator {
    private static final int MIN_AGE = 0;
    private static final int MAX_AGE = 120;
    private static final BigDecimal MAX_ANNUAL_FAMILY_INCOME = new BigDecimal("1000000000");

    public UserProfilePatch validate(UserProfilePatch patch) {
        if (patch == null) return new UserProfilePatch(null, null, null, null);
        Integer age = isValidAge(patch.age()) ? patch.age() : null;
        BigDecimal income = isValidIncome(patch.annualFamilyIncome()) ? patch.annualFamilyIncome() : null;
        return new UserProfilePatch(age, patch.state(), patch.occupation(), income);
    }

    private boolean isValidAge(Integer age) { return age == null || age >= MIN_AGE && age <= MAX_AGE; }
    private boolean isValidIncome(BigDecimal income) { return income == null || income.signum() >= 0 && income.compareTo(MAX_ANNUAL_FAMILY_INCOME) <= 0; }
}

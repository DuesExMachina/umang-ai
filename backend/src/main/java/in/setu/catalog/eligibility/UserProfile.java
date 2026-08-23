package in.setu.catalog.eligibility;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Typed, confirmed user facts consumed by the rule engine. No LLM types belong here. */
public final class UserProfile {
    private final Map<String, Object> attributes;

    private UserProfile(Map<String, Object> attributes) { this.attributes = Map.copyOf(attributes); }
    public static Builder builder() { return new Builder(); }
    public Optional<Object> attribute(String name) { return Optional.ofNullable(attributes.get(name)); }
    public boolean hasAttribute(String name) { return attributes.containsKey(name) && attributes.get(name) != null; }

    public static final class Builder {
        private final Map<String, Object> values = new LinkedHashMap<>();
        public Builder attribute(String name, Object value) {
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Profile attribute name is required");
            values.put(name, Objects.requireNonNull(value, "Profile attribute value is required"));
            return this;
        }
        public UserProfile build() { return new UserProfile(values); }
    }
}

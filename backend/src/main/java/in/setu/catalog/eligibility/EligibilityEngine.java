package in.setu.catalog.eligibility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.setu.catalog.domain.EligibilityRule;
import in.setu.catalog.domain.Scheme;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Pure deterministic evaluator for the persisted rule DSL. It neither calls nor accepts output from an LLM.
 * Supported nodes: all, any, not; and leaves with field plus equals, in, gte, lte, between, booleanIs, or exists.
 */
public final class EligibilityEngine {
    private final ObjectMapper objectMapper;

    public EligibilityEngine() { this(new ObjectMapper()); }
    EligibilityEngine(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

    public EligibilityDecision evaluate(UserProfile profile, Scheme scheme) {
        if (scheme.getEligibilityRules().isEmpty()) throw new IllegalArgumentException("Scheme has no eligibility rules: " + scheme.getCode());
        NodeResult result = NodeResult.satisfied();
        for (EligibilityRule rule : scheme.getEligibilityRules()) result = all(result, evaluateRule(profile, rule));

        EligibilityStatus status = switch (result.state) {
            case SATISFIED -> EligibilityStatus.MATCHED;
            case UNSATISFIED -> EligibilityStatus.NOT_MATCHED;
            case UNKNOWN -> result.satisfiedLeaves > 0 ? EligibilityStatus.POTENTIALLY_MATCHED : EligibilityStatus.MISSING_INFORMATION;
        };
        return new EligibilityDecision(status, List.copyOf(result.missingAttributes), List.copyOf(result.unmetRequirements));
    }

    public Map<String, EligibilityDecision> evaluate(UserProfile profile, Collection<Scheme> schemes) {
        Map<String, EligibilityDecision> decisions = new java.util.LinkedHashMap<>();
        for (Scheme scheme : schemes) decisions.put(scheme.getCode(), evaluate(profile, scheme));
        return Map.copyOf(decisions);
    }

    private NodeResult evaluateRule(UserProfile profile, EligibilityRule rule) {
        try { return evaluateNode(profile, objectMapper.readTree(rule.getRuleJson())); }
        catch (Exception exception) { throw new IllegalArgumentException("Invalid eligibility rule for scheme", exception); }
    }

    private NodeResult evaluateNode(UserProfile profile, JsonNode node) {
        requireObject(node);
        if (node.has("all")) return aggregate(profile, node.get("all"), true);
        if (node.has("any")) return aggregate(profile, node.get("any"), false);
        if (node.has("not")) return invert(evaluateNode(profile, node.get("not")));
        return evaluateLeaf(profile, node);
    }

    private NodeResult aggregate(UserProfile profile, JsonNode nodes, boolean isAll) {
        if (!nodes.isArray() || nodes.isEmpty()) throw new IllegalArgumentException("Rule group must contain at least one rule");
        NodeResult result = isAll ? NodeResult.satisfied() : NodeResult.unsatisfied("No alternative rule matched");
        for (JsonNode node : nodes) result = isAll ? all(result, evaluateNode(profile, node)) : any(result, evaluateNode(profile, node));
        return result;
    }

    private NodeResult evaluateLeaf(UserProfile profile, JsonNode node) {
        String field = requiredText(node, "field");
        Object actual = profile.attribute(field).orElse(null);
        if (actual == null) return NodeResult.unknown(field);
        if (node.has("equals")) return comparison(field, valuesEqual(actual, node.get("equals")));
        if (node.has("in")) {
            JsonNode options = node.get("in");
            if (!options.isArray()) throw new IllegalArgumentException("in must be an array");
            for (JsonNode option : options) if (valuesEqual(actual, option)) return comparison(field, true);
            return comparison(field, false);
        }
        if (node.has("gte")) return comparison(field, number(actual).compareTo(number(node.get("gte"))) >= 0);
        if (node.has("lte")) return comparison(field, number(actual).compareTo(number(node.get("lte"))) <= 0);
        if (node.has("between")) {
            JsonNode range = node.get("between");
            if (!range.isArray() || range.size() != 2) throw new IllegalArgumentException("between must contain exactly two values");
            BigDecimal candidate = number(actual);
            return comparison(field, candidate.compareTo(number(range.get(0))) >= 0 && candidate.compareTo(number(range.get(1))) <= 0);
        }
        if (node.has("booleanIs")) {
            if (!node.get("booleanIs").isBoolean() || !(actual instanceof Boolean value)) throw new IllegalArgumentException("booleanIs requires a boolean value");
            return comparison(field, value == node.get("booleanIs").booleanValue());
        }
        if (node.has("exists")) {
            if (!node.get("exists").isBoolean()) throw new IllegalArgumentException("exists must be boolean");
            return comparison(field, node.get("exists").booleanValue());
        }
        throw new IllegalArgumentException("Rule leaf needs a supported predicate");
    }

    private static NodeResult comparison(String field, boolean satisfied) { return satisfied ? NodeResult.satisfiedLeaf() : NodeResult.unsatisfied(field); }
    private static void requireObject(JsonNode node) { if (node == null || !node.isObject()) throw new IllegalArgumentException("Rule node must be an object"); }
    private static String requiredText(JsonNode node, String field) { if (!node.hasNonNull(field) || !node.get(field).isTextual()) throw new IllegalArgumentException("Rule requires text field: " + field); return node.get(field).textValue(); }
    private static BigDecimal number(Object value) { try { return value instanceof JsonNode node ? new BigDecimal(node.asText()) : new BigDecimal(value.toString()); } catch (NumberFormatException exception) { throw new IllegalArgumentException("Rule comparison requires numeric values", exception); } }
    private static boolean valuesEqual(Object actual, JsonNode expected) { return actual instanceof Number ? number(actual).compareTo(number(expected)) == 0 : actual.toString().equals(expected.asText()); }

    private static NodeResult all(NodeResult left, NodeResult right) {
        State state = left.state == State.UNSATISFIED || right.state == State.UNSATISFIED ? State.UNSATISFIED : left.state == State.UNKNOWN || right.state == State.UNKNOWN ? State.UNKNOWN : State.SATISFIED;
        return new NodeResult(state, left.satisfiedLeaves + right.satisfiedLeaves, merge(left.missingAttributes, right.missingAttributes), merge(left.unmetRequirements, right.unmetRequirements));
    }
    private static NodeResult any(NodeResult left, NodeResult right) {
        State state = left.state == State.SATISFIED || right.state == State.SATISFIED ? State.SATISFIED : left.state == State.UNKNOWN || right.state == State.UNKNOWN ? State.UNKNOWN : State.UNSATISFIED;
        return new NodeResult(state, left.satisfiedLeaves + right.satisfiedLeaves, merge(left.missingAttributes, right.missingAttributes), merge(left.unmetRequirements, right.unmetRequirements));
    }
    private static NodeResult invert(NodeResult result) { return new NodeResult(result.state == State.SATISFIED ? State.UNSATISFIED : result.state == State.UNSATISFIED ? State.SATISFIED : State.UNKNOWN, result.satisfiedLeaves, result.missingAttributes, result.unmetRequirements); }
    private static LinkedHashSet<String> merge(LinkedHashSet<String> first, LinkedHashSet<String> second) { LinkedHashSet<String> all = new LinkedHashSet<>(first); all.addAll(second); return all; }

    private enum State { SATISFIED, UNSATISFIED, UNKNOWN }
    private record NodeResult(State state, int satisfiedLeaves, LinkedHashSet<String> missingAttributes, LinkedHashSet<String> unmetRequirements) {
        static NodeResult satisfied() { return new NodeResult(State.SATISFIED, 0, new LinkedHashSet<>(), new LinkedHashSet<>()); }
        static NodeResult satisfiedLeaf() { return new NodeResult(State.SATISFIED, 1, new LinkedHashSet<>(), new LinkedHashSet<>()); }
        static NodeResult unsatisfied(String requirement) { return new NodeResult(State.UNSATISFIED, 0, new LinkedHashSet<>(), new LinkedHashSet<>(List.of(requirement))); }
        static NodeResult unknown(String attribute) { return new NodeResult(State.UNKNOWN, 0, new LinkedHashSet<>(List.of(attribute)), new LinkedHashSet<>()); }
    }
}

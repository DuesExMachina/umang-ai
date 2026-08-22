package in.setu.catalog.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "eligibility_rule")
public class EligibilityRule {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "scheme_id") private Scheme scheme;
    @Column(nullable = false, columnDefinition = "text") private String ruleJson;
    @Column(nullable = false) private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    protected EligibilityRule() { }
    public UUID getId() { return id; } public String getRuleJson() { return ruleJson; } public LocalDate getEffectiveFrom() { return effectiveFrom; } public LocalDate getEffectiveTo() { return effectiveTo; }
}

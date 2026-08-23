package in.setu.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "scheme")
public class Scheme {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 80) private String code;
    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, columnDefinition = "text") private String summary;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private SchemeCategory category;
    @Column(nullable = false) private boolean syntheticDemo;
    @Column(nullable = false, columnDefinition = "text") private String disclaimer;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private PublicationStatus status;
    @Column(nullable = false) private int cataloguePriority;
    @Column(nullable = false, length = 30) private String version;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    @OneToMany(mappedBy = "scheme", cascade = CascadeType.ALL, orphanRemoval = true) private List<EligibilityRule> eligibilityRules = new ArrayList<>();
    @OneToMany(mappedBy = "scheme", cascade = CascadeType.ALL, orphanRemoval = true) private List<RequiredDocument> requiredDocuments = new ArrayList<>();
    @OneToOne(mappedBy = "scheme", cascade = CascadeType.ALL, orphanRemoval = true) private ApplicationInformation applicationInformation;
    @OneToMany(mappedBy = "scheme", cascade = CascadeType.ALL, orphanRemoval = true) private List<SchemeSourceInformation> sourceInformation = new ArrayList<>();

    protected Scheme() { }
    public Scheme(UUID id, String code, String title, String summary, SchemeCategory category, boolean syntheticDemo, String disclaimer, PublicationStatus status, int cataloguePriority, String version) {
        this.id = id; this.code = code; this.title = title; this.summary = summary; this.category = category; this.syntheticDemo = syntheticDemo; this.disclaimer = disclaimer; this.status = status; this.cataloguePriority = cataloguePriority; this.version = version;
    }
    @PrePersist void onCreate() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; } public String getCode() { return code; } public String getTitle() { return title; } public String getSummary() { return summary; } public SchemeCategory getCategory() { return category; } public boolean isSyntheticDemo() { return syntheticDemo; } public String getDisclaimer() { return disclaimer; } public PublicationStatus getStatus() { return status; } public int getCataloguePriority() { return cataloguePriority; } public String getVersion() { return version; }
    public List<EligibilityRule> getEligibilityRules() { return List.copyOf(eligibilityRules); } public List<RequiredDocument> getRequiredDocuments() { return List.copyOf(requiredDocuments); } public ApplicationInformation getApplicationInformation() { return applicationInformation; } public List<SchemeSourceInformation> getSourceInformation() { return List.copyOf(sourceInformation); }
    public void addEligibilityRule(EligibilityRule rule) { rule.attachTo(this); eligibilityRules.add(rule); }
}

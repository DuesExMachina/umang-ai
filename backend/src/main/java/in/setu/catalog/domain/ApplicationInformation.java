package in.setu.catalog.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "application_information")
public class ApplicationInformation {
    @Id private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "scheme_id", unique = true) private Scheme scheme;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private ApplicationMode applicationMode;
    @Column(nullable = false, columnDefinition = "text") private String instructions;
    private String applicationUrl;
    @Column(columnDefinition = "text") private String contactInformation;
    protected ApplicationInformation() { }
    public UUID getId() { return id; } public ApplicationMode getApplicationMode() { return applicationMode; } public String getInstructions() { return instructions; } public String getApplicationUrl() { return applicationUrl; } public String getContactInformation() { return contactInformation; }
}

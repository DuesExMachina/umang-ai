package in.setu.catalog.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "scheme_source_information")
public class SchemeSourceInformation {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "scheme_id") private Scheme scheme;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private SourceType sourceType;
    @Column(nullable = false, length = 200) private String sourceName;
    @Column(nullable = false, columnDefinition = "text") private String sourceReference;
    private String sourceUrl;
    private LocalDate verifiedOn;
    protected SchemeSourceInformation() { }
    public UUID getId() { return id; } public SourceType getSourceType() { return sourceType; } public String getSourceName() { return sourceName; } public String getSourceReference() { return sourceReference; } public String getSourceUrl() { return sourceUrl; } public LocalDate getVerifiedOn() { return verifiedOn; }
}

package in.setu.catalog.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "required_document")
public class RequiredDocument {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "scheme_id") private Scheme scheme;
    @Column(nullable = false, length = 200) private String name;
    @Column(nullable = false, columnDefinition = "text") private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private DocumentRequiredness requiredness;
    @Column(columnDefinition = "text") private String conditionJson;
    @Column(nullable = false) private int displayOrder;
    protected RequiredDocument() { }
    public UUID getId() { return id; } public String getName() { return name; } public String getDescription() { return description; } public DocumentRequiredness getRequiredness() { return requiredness; } public String getConditionJson() { return conditionJson; } public int getDisplayOrder() { return displayOrder; }
}

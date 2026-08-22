package in.setu.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import in.setu.catalog.domain.PublicationStatus;
import in.setu.catalog.domain.Scheme;
import in.setu.catalog.domain.SchemeCategory;
import in.setu.catalog.repository.SchemeRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class SchemeCatalogueServiceTest {
    @Mock private SchemeRepository schemeRepository;
    private SchemeCatalogueService service;

    @BeforeEach void setUp() { service = new SchemeCatalogueService(schemeRepository); }

    @Test
    void filtersByCategoryWhenOneIsProvided() {
        var education = scheme("DEMO-EDU-001", SchemeCategory.EDUCATION);
        when(schemeRepository.findByCategoryAndStatusOrderByCataloguePriorityDescTitleAsc(SchemeCategory.EDUCATION, PublicationStatus.PUBLISHED)).thenReturn(List.of(education));

        var results = service.findPublishedSchemes(SchemeCategory.EDUCATION);

        assertThat(results).containsExactly(education);
        verify(schemeRepository).findByCategoryAndStatusOrderByCataloguePriorityDescTitleAsc(SchemeCategory.EDUCATION, PublicationStatus.PUBLISHED);
        verifyNoMoreInteractions(schemeRepository);
    }

    @Test
    void returnsAllPublishedSchemesWhenCategoryIsNotSelected() {
        when(schemeRepository.findByStatusOrderByCataloguePriorityDescTitleAsc(PublicationStatus.PUBLISHED)).thenReturn(List.of(scheme("DEMO-HLT-001", SchemeCategory.HEALTHCARE)));

        assertThat(service.findPublishedSchemes(null)).hasSize(1);
        verify(schemeRepository).findByStatusOrderByCataloguePriorityDescTitleAsc(PublicationStatus.PUBLISHED);
    }

    @Test
    void rejectsUnknownOrUnpublishedCodes() {
        when(schemeRepository.findByCodeAndStatus("DEMO-MISSING", PublicationStatus.PUBLISHED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findPublishedScheme("DEMO-MISSING"))
            .isInstanceOf(SchemeNotFoundException.class)
            .hasMessageContaining("DEMO-MISSING");
    }

    private Scheme scheme(String code, SchemeCategory category) {
        return new Scheme(UUID.randomUUID(), code, "Synthetic " + code, "Synthetic test data.", category, true,
            "Synthetic demo scheme only.", PublicationStatus.PUBLISHED, 1, "test-1");
    }
}

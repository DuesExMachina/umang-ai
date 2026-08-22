package in.setu.catalog.repository;

import static org.assertj.core.api.Assertions.assertThat;

import in.setu.catalog.domain.PublicationStatus;
import in.setu.catalog.domain.Scheme;
import in.setu.catalog.domain.SchemeCategory;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop", "spring.datasource.url=jdbc:h2:mem:catalog;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"})
class SchemeRepositoryTest {
    @Autowired private SchemeRepository schemeRepository;

    @Test
    void findsOnlyPublishedSchemesInRequestedCategoryInPriorityOrder() {
        schemeRepository.save(scheme("DEMO-EDU-LOW", "Zebra", SchemeCategory.EDUCATION, PublicationStatus.PUBLISHED, 1));
        schemeRepository.save(scheme("DEMO-EDU-HIGH", "Alpha", SchemeCategory.EDUCATION, PublicationStatus.PUBLISHED, 10));
        schemeRepository.save(scheme("DEMO-EDU-DRAFT", "Draft", SchemeCategory.EDUCATION, PublicationStatus.DRAFT, 99));
        schemeRepository.save(scheme("DEMO-HLT-001", "Health", SchemeCategory.HEALTHCARE, PublicationStatus.PUBLISHED, 99));

        var result = schemeRepository.findByCategoryAndStatusOrderByCataloguePriorityDescTitleAsc(SchemeCategory.EDUCATION, PublicationStatus.PUBLISHED);

        assertThat(result).extracting(Scheme::getCode).containsExactly("DEMO-EDU-HIGH", "DEMO-EDU-LOW");
        assertThat(result).allMatch(Scheme::isSyntheticDemo);
    }

    @Test
    void doesNotExposeDraftSchemeThroughPublishedLookup() {
        schemeRepository.save(scheme("DEMO-DRAFT", "Draft", SchemeCategory.EMPLOYMENT, PublicationStatus.DRAFT, 1));

        assertThat(schemeRepository.findByCodeAndStatus("DEMO-DRAFT", PublicationStatus.PUBLISHED)).isEmpty();
    }

    private Scheme scheme(String code, String title, SchemeCategory category, PublicationStatus status, int priority) {
        return new Scheme(UUID.randomUUID(), code, title, "Synthetic test data.", category, true,
            "Synthetic demo scheme only.", status, priority, "test-1");
    }
}

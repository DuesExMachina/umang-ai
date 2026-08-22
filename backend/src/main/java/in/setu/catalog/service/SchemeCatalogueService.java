package in.setu.catalog.service;

import in.setu.catalog.domain.PublicationStatus;
import in.setu.catalog.domain.Scheme;
import in.setu.catalog.domain.SchemeCategory;
import in.setu.catalog.repository.SchemeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SchemeCatalogueService {
    private final SchemeRepository schemeRepository;

    public SchemeCatalogueService(SchemeRepository schemeRepository) { this.schemeRepository = schemeRepository; }

    public List<Scheme> findPublishedSchemes(SchemeCategory category) {
        return category == null
            ? schemeRepository.findByStatusOrderByCataloguePriorityDescTitleAsc(PublicationStatus.PUBLISHED)
            : schemeRepository.findByCategoryAndStatusOrderByCataloguePriorityDescTitleAsc(category, PublicationStatus.PUBLISHED);
    }

    public Scheme findPublishedScheme(String code) {
        return schemeRepository.findByCodeAndStatus(code, PublicationStatus.PUBLISHED)
            .orElseThrow(() -> new SchemeNotFoundException(code));
    }
}

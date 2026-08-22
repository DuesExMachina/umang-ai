package in.setu.catalog.repository;

import in.setu.catalog.domain.PublicationStatus;
import in.setu.catalog.domain.Scheme;
import in.setu.catalog.domain.SchemeCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchemeRepository extends JpaRepository<Scheme, UUID> {
    List<Scheme> findByStatusOrderByCataloguePriorityDescTitleAsc(PublicationStatus status);
    List<Scheme> findByCategoryAndStatusOrderByCataloguePriorityDescTitleAsc(SchemeCategory category, PublicationStatus status);
    Optional<Scheme> findByCodeAndStatus(String code, PublicationStatus status);
}

package propensi.smail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.smail.model.TemplateSurat;

@Repository
public interface TemplateSuratDb extends JpaRepository<TemplateSurat, String> {
}

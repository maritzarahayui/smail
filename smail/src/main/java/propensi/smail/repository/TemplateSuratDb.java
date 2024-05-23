package propensi.smail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.smail.model.TemplateSurat;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateSuratDb extends JpaRepository<TemplateSurat, String> {
    List<TemplateSurat> findByIsActiveTrue();

    long countByKategori(String kategori);

    List<TemplateSurat> findByNamaTemplateContainingIgnoreCaseAndIsActiveIsTrue(String namaTemplate);

    List<String> findNamaTemplateByKategori(String kategori);

    TemplateSurat findByRequestTemplate(String requestTemplate);

    TemplateSurat findByNamaTemplate(String nama);

    Optional<TemplateSurat> findById(String id);

}

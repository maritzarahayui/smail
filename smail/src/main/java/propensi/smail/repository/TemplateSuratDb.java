package propensi.smail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.smail.model.TemplateSurat;

import java.util.List;

@Repository
public interface TemplateSuratDb extends JpaRepository<TemplateSurat, String> {

    List<TemplateSurat> findByIsActiveTrue();

    long countByKategori(String kategori);

    List<TemplateSurat> findByNamaTemplateContainingIgnoreCase(String namaTemplate);
}

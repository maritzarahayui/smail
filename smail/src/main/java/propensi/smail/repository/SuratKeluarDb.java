package propensi.smail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.smail.model.SuratKeluar;

@Repository
public interface SuratKeluarDb extends JpaRepository<SuratKeluar, String> {
    long countByKategori(String kategori);
}

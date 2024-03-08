package propensi.smail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.smail.model.SuratMasuk;

@Repository
public interface SuratMasukDb extends JpaRepository<SuratMasuk, String> {
    long countByKategori(String kategori);
}

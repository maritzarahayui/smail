package propensi.smail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.smail.model.SuratMasuk;

import java.util.List;
import java.util.Date;

@Repository
public interface SuratMasukDb extends JpaRepository<SuratMasuk, String> {
    long countByKategori(String kategori);
    List<SuratMasuk> findByTanggalDibuat(Date tanggalDibuat);
    List<SuratMasuk> findByStatus(int status);
}

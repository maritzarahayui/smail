package propensi.smail.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.smail.model.SuratMasuk;

@Repository
public interface SuratMasukDb extends JpaRepository<SuratMasuk, String> {
    long countByKategori(String kategori);
    List<SuratMasuk> findByTanggalDibuat(Date tanggalDibuat);
    List<SuratMasuk> findByStatus(int status);
}

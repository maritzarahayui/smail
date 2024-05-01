package propensi.smail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.SuratKeluar;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface SuratKeluarDb extends JpaRepository<SuratKeluar, String> {
    long countByKategori(String kategori);
    List<SuratKeluar> findByPenandatanganId(String penandatanganId);
    SuratKeluar findByRequestSurat(Optional<RequestSurat> requestSurat);
    SuratKeluar findByNomorArsipContainingIgnoreCase(String nomorArsip);
    List<SuratKeluar> findByTanggalDibuat(Date tanggalDibuat);
    List<SuratKeluar> findByTanggalDibuatBetween(Date tanggalAwal, Date tanggalAkhir);
}

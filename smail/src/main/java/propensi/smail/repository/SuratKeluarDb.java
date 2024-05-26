package propensi.smail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.SuratKeluar;
import propensi.smail.model.user.Pengguna;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface SuratKeluarDb extends JpaRepository<SuratKeluar, String> {
    long countByKategori(String kategori);
    List<SuratKeluar> findByKategori(String kategori);
    List<SuratKeluar> findByPenandatanganId(String penandatanganId);
    SuratKeluar findByRequestSurat(Optional<RequestSurat> requestSurat);
    SuratKeluar findByNomorArsipContainingIgnoreCase(String nomorArsip);
    List<SuratKeluar> findByTanggalDibuat(Date tanggalDibuat);
    List<SuratKeluar> findByCurrentPenandatanganAndIsSigned(Pengguna penandatangan, Boolean isSigned);
    List<SuratKeluar> findByIsSigned(Boolean isSigned);
    SuratKeluar findByNomorArsip(String nomorArsip);
    List<SuratKeluar> findByCurrentPenandatangan(Pengguna penandatangan);
    List<SuratKeluar> findByCurrentPenandatanganOrderByIsSignedAscTanggalDibuatDesc(Pengguna penandatangan);
    List<SuratKeluar> findByTanggalDibuatBetween(Date tanggalAwal, Date tanggalAkhir);
    List<SuratKeluar> findByPenandatanganContainsAndIsSigned(Pengguna penandatangan, Boolean isSigned);
}

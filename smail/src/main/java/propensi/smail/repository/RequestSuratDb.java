package propensi.smail.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import propensi.smail.model.SuratKeluar;
import propensi.smail.model.user.*;
import propensi.smail.model.RequestSurat;

@Repository
public interface RequestSuratDb extends JpaRepository<RequestSurat, String> {
    // long countByPengajuRole(Role role);
    // List<RequestSurat> findByPengaju(Pengguna pengaju);
    @Query("SELECT COUNT(rs) FROM RequestSurat rs WHERE rs.pengaju.id IN (SELECT d.id FROM Dosen d)")
    long countRequestByDosen();

    @Query("SELECT COUNT(rs) FROM RequestSurat rs WHERE rs.pengaju.id IN (SELECT s.id FROM Staf s)")
    long countRequestByStaf();

    @Query("SELECT COUNT(rs) FROM RequestSurat rs WHERE rs.pengaju.id IN (SELECT m.id FROM Mahasiswa m)")
    long countRequestByMahasiswa();

    List<RequestSurat> findByStatus(int status);
    RequestSurat findByIdContainingIgnoreCase(String id);
    List<RequestSurat> findByJenisSuratContainingIgnoreCase(String jenisSurat);
    List<RequestSurat> findByBentukSuratContainingIgnoreCase(String bentukSurat);
    List<RequestSurat> findByTanggalPengajuan(Date tanggalPengajuan);
    List<RequestSurat> findByTanggalDibatalkan(Date tanggalDibatalkan);
    List<RequestSurat> findByTanggalPenolakan(Date tanggalPenolakan);
    List<RequestSurat> findByTanggalSelesai(Date tanggalSelesai);
    List<RequestSurat> findByTanggalPengajuanOrTanggalDibatalkan(Date tanggalPengajuan, Date tanggalDibatalkan);
    List<RequestSurat> findByTanggalPengajuanOrTanggalPenolakan(Date tanggalPengajuan, Date tanggalPenolakan);
    List<RequestSurat> findByTanggalPengajuanOrTanggalSelesai(Date tanggalPengajuan, Date tanggalSelesai);
}

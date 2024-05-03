package propensi.smail.repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import propensi.smail.model.SuratKeluar;
import propensi.smail.model.user.*;
import propensi.smail.model.RequestSurat;

@Repository
public interface RequestSuratDb extends JpaRepository<RequestSurat, String> {
    // long countByPengajuRole(Role role);
    @Query("SELECT COUNT(rs) FROM RequestSurat rs WHERE rs.pengaju.id IN (SELECT d.id FROM Dosen d)")
    long countRequestByDosen();

    @Query("SELECT COUNT(rs) FROM RequestSurat rs WHERE rs.pengaju.id IN (SELECT s.id FROM Staf s)")
    long countRequestByStaf();

    @Query("SELECT COUNT(rs) FROM RequestSurat rs WHERE rs.pengaju.id IN (SELECT m.id FROM Mahasiswa m)")
    long countRequestByMahasiswa();

    List<RequestSurat> findByStatus(int status);
    RequestSurat findByIdContainingIgnoreCase(String id);
    List<RequestSurat> findByJenisSuratContainingIgnoreCase(String jenisSurat);
    List<RequestSurat> findByTanggalPengajuan(Date tanggalPengajuan);
    List<RequestSurat> findByTanggalDibatalkan(Date tanggalDibatalkan);
    List<RequestSurat> findByTanggalPenolakan(Date tanggalPenolakan);
    List<RequestSurat> findByTanggalSelesai(Date tanggalSelesai);
    List<RequestSurat> findByTanggalPengajuanOrTanggalDibatalkan(Date tanggalPengajuan, Date tanggalDibatalkan);
    List<RequestSurat> findByTanggalPengajuanOrTanggalPenolakan(Date tanggalPengajuan, Date tanggalPenolakan);
    List<RequestSurat> findByTanggalPengajuanOrTanggalSelesai(Date tanggalPengajuan, Date tanggalSelesai);

    List<RequestSurat> findByStatusAndPengajuId(int status, String penggunaId);

    @Query("SELECT r FROM RequestSurat r WHERE " +
            "LOWER(r.pengaju.nama) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.jenisSurat) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.id) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<RequestSurat> findByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT r FROM RequestSurat r WHERE (" +
    "LOWER(r.pengaju.nama) LIKE LOWER(CONCAT('%', :search, '%')) OR " + 
    "LOWER(r.jenisSurat) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
    "LOWER(r.id) LIKE LOWER(CONCAT('%', :search, '%'))) AND " + 
    "r.status = :status AND r.pengaju.id = :pengajuId")
    List<RequestSurat> findBySearchAndStatusAndPengajuId(@Param("search") String search, @Param("status") int status, @Param("pengajuId") String pengajuId);
    
    @Query("SELECT r FROM RequestSurat r WHERE EXTRACT(YEAR FROM r.tanggalPengajuan) = :year AND EXTRACT(MONTH FROM r.tanggalPengajuan) = :month")
    List<RequestSurat> findByTanggalPengajuanMonthly(Integer month, Integer year);
    @Query("SELECT r.pengaju.nama FROM RequestSurat r GROUP BY r.pengaju.id, r.pengaju.nama ORDER BY COUNT(r) DESC LIMIT 1")
    String findTopRequester();
    // emi sprint 3
    List<RequestSurat> findByPengaju(Pengguna pengaju);
    long countByPengajuAndStatus(Pengguna pengaju, int status);
    List<String> findDistinctKategoriByPengaju(Pengguna pengaju);
    long countByKategoriAndPengaju(String kategori, Pengguna pengaju);
    List<String> findDistinctJenisByPengaju(Pengguna pengaju);
    long countByJenisSuratAndPengaju(String jenis, Pengguna pengaju);
}

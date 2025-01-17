package propensi.smail.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import propensi.smail.model.SuratMasuk;

@Repository
public interface SuratMasukDb extends JpaRepository<SuratMasuk, String> {
   long countByKategori(String kategori);
   List<SuratMasuk> findByTanggalDibuat(Date tanggalDibuat);

   @Query("SELECT s FROM SuratMasuk s WHERE " +
      "(LOWER(s.nomorArsip) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.kategori) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.perihal) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.pengirim) LIKE LOWER(CONCAT('%', :search, '%')))")
   List<SuratMasuk> findBySearch(@Param("search") String search);
   
   @Query("SELECT s FROM SuratMasuk s WHERE " +
      "(LOWER(s.nomorArsip) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.kategori) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.perihal) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.pengirim) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
      "s.isDisposisi = true")
   List<SuratMasuk> findBySearchAndIsDisposisi(@Param("search") String search);

   @Query("SELECT s FROM SuratMasuk s WHERE " +
   "(LOWER(s.nomorArsip) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
   "LOWER(s.kategori) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
   "LOWER(s.perihal) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
   "LOWER(s.pengirim) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
   "s.isFollowUp = true")
   List<SuratMasuk> findBySearchAndIsFollowUp(@Param("search") String search);

   List<SuratMasuk> findByIsFollowUpTrue();
   List<SuratMasuk> findByIsDisposisiTrue();
   List<SuratMasuk> findByTanggalDibuatBetween(Date tanggalAwal, Date tanggalAkhir);

}

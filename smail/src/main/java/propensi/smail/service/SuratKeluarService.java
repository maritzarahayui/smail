package propensi.smail.service;

import org.springframework.web.multipart.MultipartFile;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.SuratKeluar;
import propensi.smail.model.user.Pengguna;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface SuratKeluarService {
    List<SuratKeluar> getAllSuratKeluar();
    SuratKeluar storeTtd(RequestSurat requestSurat, MultipartFile file, String kategori, String jenisSurat, List<Pengguna> penandatangan) throws IOException;
    String generateId(String kategori);
    SuratKeluar getFileTtd(String id);
    void updateSuratKeluarFile(String id, MultipartFile file);
    SuratKeluar findSuratKeluarByRequestID(String id);
    SuratKeluar findSuratKeluarByNomorArsip(String nomorArsip);
    void update(SuratKeluar suratKeluar);

    SuratKeluar storeInput(MultipartFile file, String kategori, String perihal, String penerimaEkesternal);
    Stream<SuratKeluar> getAllFiles();
    SuratKeluar getFileInput(String id);
    Map<String, List<String>> generateJenisSuratByKategoriAndRole(String tipePengaju);
    List<SuratKeluar> searchSuratKeluar(Map<String, String> params, Date tanggalDibuat, String sort, String searchQuery);

    SuratKeluar findSuratKeluarByID(String id);

    /* DASHBOARD */
    Map<String, Long> getJumlahSuratKeluarPerKategori();
    Map<String, Integer> getJumlahSuratKeluarTahunIni();
    Map<String, Integer> getJumlahSuratKeluarBulanIni();
    Map<String, Integer> getJumlahSuratKeluarMingguIni();


}

package propensi.smail.service;

import org.springframework.web.multipart.MultipartFile;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.SuratKeluar;
import propensi.smail.model.user.Pengguna;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface SuratKeluarService {
    List<SuratKeluar> getAllSuratKeluar();
    SuratKeluar store(RequestSurat requestSurat, MultipartFile file, String kategori, String jenisSurat, Pengguna penandatangan) throws IOException;
    String generateId(String kategori);
    SuratKeluar getFile(String id);
    void updateSuratKeluarFile(String id, MultipartFile file);
    SuratKeluar findSuratKeluarByRequestID(String id);
    SuratKeluar findSuratKeluarByNomorArsip(String nomorArsip);
    void update(SuratKeluar suratKeluar);
}

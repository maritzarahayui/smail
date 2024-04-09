package propensi.smail.service;

import org.springframework.web.multipart.MultipartFile;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.SuratKeluar;

import java.io.IOException;
import java.util.Date;

public interface SuratKeluarService {
    SuratKeluar store(RequestSurat requestSurat, MultipartFile file, String kategori, String jenisSurat) throws IOException;
    String generateId(String kategori);
    SuratKeluar getFile(String id);
}

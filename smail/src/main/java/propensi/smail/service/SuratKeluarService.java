package propensi.smail.service;

import java.io.IOException;
import java.util.Date;
import java.util.stream.Stream;
import java.util.List ;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import propensi.smail.model.SuratKeluar;
public interface SuratKeluarService {
    SuratKeluar store(MultipartFile file, String kategori, String jenisSurat, String perihal, String pengirim);
    SuratKeluar getFile(String id);
    Stream<SuratKeluar> getAllFiles();
    List<SuratKeluar> getAllSuratKeluar();
    Map<String, List<String>> generateJenisSuratByKategoriAndRole(String tipePengaju);
    String generateId(String kategori);
    List<SuratKeluar> searchSuratKeluar(Map<String, String> params, Date tanggalDibuat, String sort, String searchQuery);
}

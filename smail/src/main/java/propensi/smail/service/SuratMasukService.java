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

import propensi.smail.model.SuratMasuk;
public interface SuratMasukService {
    SuratMasuk store(MultipartFile file, String kategori, String perihal, String pengirim, String tembusan);
    SuratMasuk getFile(String id);
    Stream<SuratMasuk> getAllFiles();
    List<SuratMasuk> getAllSuratMasuk();
    String generateId(String kategori);
    List<SuratMasuk> searchSuratMasuk(Map<String, String> params, Date tanggalDibuat, String sort, String searchQuery);
    List<SuratMasuk> getSuratMasukByStatus(int status);
}

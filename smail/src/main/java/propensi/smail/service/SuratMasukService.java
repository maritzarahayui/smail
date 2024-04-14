package propensi.smail.service;

import propensi.smail.model.SuratMasuk;
import java.io.IOException;
import java.util.Date;
import java.util.stream.Stream;
import java.util.List ;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

public interface SuratMasukService {
    SuratMasuk storeJudul(MultipartFile file, String kategori, String perihal, String pengirim, String judul);
    SuratMasuk getFile(String id);
    Stream<SuratMasuk> getAllFiles();
    List<SuratMasuk> getAllSuratMasuk();
    String generateId(String kategori);
    void sendEmail(String[] to, String subject, String body, SuratMasuk suratMasuk) throws MessagingException, IOException;

    SuratMasuk store(MultipartFile file, String kategori, String perihal, String pengirim);
    List<SuratMasuk> searchSuratMasuk(Map<String, String> params, Date tanggalDibuat, String sort, String searchQuery);
    List<SuratMasuk> getSuratMasukByStatus(int status);
}

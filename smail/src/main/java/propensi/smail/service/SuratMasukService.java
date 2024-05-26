package propensi.smail.service;

import propensi.smail.model.*;
import propensi.smail.model.user.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public interface SuratMasukService {
    SuratMasuk storeJudul(MultipartFile file, String kategori, String perihal, String pengirim, String judul); //gak kepake kyknya
    SuratMasuk getFile(String id);
    Stream<SuratMasuk> getAllFiles();
    List<SuratMasuk> getAllSuratMasuk();
    String generateId(String kategori);
    void sendEmail(String[] to, String subject, String body, SuratMasuk suratMasuk) throws MessagingException, IOException;

    SuratMasuk store(MultipartFile file, String kategori, String perihal, String pengirim);
    List<Pengguna> getAllPenandatangan();

    List<SuratMasuk> getSuratMasukIsDisposisi();
    List<SuratMasuk> getSuratMasukIsFollowUp();

    /* DASHBOARD */
    Map<String, Long> getJumlahSuratMasukPerKategori();
    Map<String, Integer> getJumlahSuratMasukTahunIni();
    Map<String, Integer> getJumlahSuratMasukBulanIni();
    Map<String, Integer> getJumlahSuratMasukMingguIni();

}

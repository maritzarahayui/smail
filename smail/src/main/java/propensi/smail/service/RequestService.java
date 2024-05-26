package propensi.smail.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import propensi.smail.model.SuratKeluar;
import propensi.smail.model.user.*;
import propensi.smail.dto.RequestAndFieldDataDTO;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.RequestTemplate;

public interface RequestService {
    // REQUEST SURAT 
    void saveOrUpdate(RequestSurat requestSurat);
    RequestSurat createRequestSurat(RequestSurat requestSurat, RequestAndFieldDataDTO requestDTO);
    
    List<RequestSurat> getAllRequestsSurat();
    List<RequestSurat> getAllSubmitedRequestsSurat();
    List<RequestSurat> getAllCanceledRequestsSurat();
    List<RequestSurat> getAllRejectedRequestsSurat();
    List<RequestSurat> getAllOnProcessRequestsSurat();
    List<RequestSurat> getAllFinishedRequestsSurat();
    List<RequestSurat> getAllSubmittedRequestsSuratByPengaju(String penggunaId);
    List<RequestSurat> getAllCancelledRequestsSuratByPengaju(String penggunaId);
    List<RequestSurat> getAllRejectedRequestsSuratByPengaju(String penggunaId);
    List<RequestSurat> getAllOnProcessRequestsSuratByPengaju(String penggunaId);
    List<RequestSurat> getAllFinishedRequestsSuratByPengaju(String penggunaId);
    List<RequestSurat> getAllRequestSuratByPenandatanganId(String penandatanganId);

    RequestSurat getRequestSuratById(String requestSuratId);
    RequestSurat findRequestById(String id);
    RequestSurat batalkanRequestSurat(String requestSuratId, String alasanPembatalan);

    int countAllRequests();
    Map<String, List<String>> generateJenisSuratByKategoriAndRole(String tipePengaju);
    Map<Integer, String> listBentukSurat();
    Map<Integer, String> listBahasa();

    void sendEmailRejection(String to, String subject, String body, RequestSurat requestSurat) throws MessagingException, IOException;
    void sendEmailFinished(String to, String subject, String body, RequestSurat requestSurat, SuratKeluar suratKeluar) throws MessagingException, IOException;
    String generateRequestId(Pengguna pengaju);

    // REQUEST TEMPLATE
    void createRequestTemplate(RequestTemplate requestTemplate, RequestAndFieldDataDTO requestDTO);
    List<RequestTemplate> retrieveAllRequest();
    Map<Integer, String> listKategori();
    RequestTemplate store(MultipartFile file);
    RequestTemplate getFile(String id);
    RequestTemplate getRequestTemplateById(String requestSuratId);

    // PREVIEW TEMPLATE
    List<String> getAllJenisByKategori(String Kategori);

    // DASHBOARD 
    Integer countDurasi(RequestSurat requestSurat);
    Integer countAveragePerforma(List<RequestSurat> listRequestSurat, String kategori);
    Map<String, Integer> getPerformaRequestSurat();
    Map<String, Map<String, Long>> getJumlahRequestPerMinggu();
    Map<String, Long> getJumlahRequestPerMonth();
    Map<String, Long> getJumlahRequestPerYear();
    Map<String, Long> getJumlahRequestByKategori();
    Map<String, Long> getJumlahRequestByRole();
    String getTopRequester();
    String getCurrentYearMonth();
    Map<String, Map<String, Long>> getJumlahRequestPerYearAndMonth();
    List<RequestSurat> getRequestSuratByKategori(String kategori);
    Map<String, Long> getJumlahRequestPerMonthByUser(Pengguna pengguna);
    Map<String, Long> getJumlahRequestPerMingguByUser(Pengguna pengguna);
    Map<String, Long> getJumlahRequestPerYearByUser(Pengguna pengguna);
    Map<String, Long> getJumlahRequestByStatus(Pengguna pengguna); 
    Map<String, Long> getCountOfRequestByCategory(Pengguna pengguna); 
    Map<String, Long> getCountOfRequestByJenis(Pengguna pengguna); 
    
}


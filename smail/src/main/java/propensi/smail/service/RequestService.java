package propensi.smail.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import propensi.smail.model.user.*;
import propensi.smail.dto.RequestAndFieldDataDTO;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.RequestTemplate;

public interface RequestService {
    // METHOD REQUEST SURAT
    void saveOrUpdate(RequestSurat requestSurat);
    RequestSurat createRequestSurat(RequestSurat requestSurat, RequestAndFieldDataDTO requestDTO);
    List<RequestSurat> getAllRequestsSurat();
    List<RequestSurat> searchRequests(String keyword, int status);
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
    List<RequestSurat> getRequestByJenisSurat(String jenisSurat);
    List<RequestSurat> getRequestByTanggalPengajuan(Date tanggalPengajuan);
    List<RequestSurat> getRequestByTanggalDibatalkan(Date tanggalDibatalkan);
    List<RequestSurat> getRequestByTanggalPenolakan(Date tanggalPenolakan);
    List<RequestSurat> getRequestByTanggalSelesai(Date tanggalSelesai);
    List<RequestSurat> getRequestByTanggalPengajuanOrTanggalDibatalkan(Date tanggalPengajuan, Date tanggalDibatalkan);
    List<RequestSurat> getRequestByTanggalPengajuanOrTanggalPenolakan(Date tanggalPengajuan, Date tanggalPenolakan);
    List<RequestSurat> getRequestByTanggalPengajuanOrTanggalSelesai(Date tanggalPengajuan, Date tanggalSelesai);
    RequestSurat batalkanRequestSurat(String requestSuratId, String alasanPembatalan);
    int countAllRequests();
    Map<String, List<String>> generateJenisSuratByKategoriAndRole(String tipePengaju);
    Map<Integer, String> listBentukSurat();
    Map<Integer, String> listBahasa();
    // List<String> getJenisSuratForKategori(String kategori);
    // List<RequestSurat> getRequestsByUser(Pengguna pengguna);

    String generateRequestId(Pengguna pengaju);

    // METHOD REQUEST TEMPLATE
    void createRequestTemplate(RequestTemplate requestTemplate, RequestAndFieldDataDTO requestDTO);
    List<RequestTemplate> retrieveAllRequest();
    Map<Integer, String> listKategori();

    // PREVIEW TEMPLATE
    List<String> getAllJenisByKategori(String Kategori);

    List<RequestSurat> getBySearchAndStatusAndPengaju(int status, String search, String pengaju);

    /* DASHBOARD */
    Integer countDurasi(RequestSurat requestSurat);
    Integer countAveragePerforma(List<RequestSurat> listRequestSurat);
    Map<String, Integer> getPerformaRequestSurat();
    Map<String, Long> getJumlahRequestPerMinggu();
    Map<String, Long> getJumlahRequestPerMonth();
    Map<String, Long> getJumlahRequestPerYear();
    Map<String, Long> getJumlahRequestByKategori();
    Map<String, Long> getJumlahRequestByRole();
    String getTopRequester();

    //  EMIIIIIIIIIII ////
    Map<String, Long> getJumlahRequestPerMonthByUser(Pengguna pengguna);
    Map<String, Long> getJumlahRequestPerMingguByUser(Pengguna pengguna);
    Map<String, Long> getJumlahRequestPerYearByUser(Pengguna pengguna);

    Map<String, Long> getJumlahRequestByStatus(Pengguna pengguna); //ok
    Map<String, Long> getCountOfRequestByCategory(Pengguna pengguna); // ok //dosen & staf
    Map<String, Long> getCountOfRequestByJenis(Pengguna pengguna); //ok // mahasiswa

    // long countRequestsSignedByDosen();
    
    
}


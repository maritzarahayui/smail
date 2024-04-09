package propensi.smail.service;

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
    RequestSurat getRequestSuratById(String requestSuratId);
    RequestSurat batalkanRequestSurat(String requestSuratId);
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
}


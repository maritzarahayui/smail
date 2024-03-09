package propensi.smail.service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import propensi.smail.model.Pengguna;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.RequestTemplate;
import propensi.smail.model.TemplateSurat;

public interface RequestService {
    RequestSurat createRequestSurat(RequestSurat requestSurat);
    List<RequestSurat> getAllRequestsSurat();
    // List<RequestSurat> getRequestsByUser(Pengguna pengguna);
    RequestSurat getRequestSuratById(String requestSuratId);
    RequestSurat batalkanRequestSurat(String requestSuratId);
    // Map<Integer, String> getListUserType();
    int countAllRequests();
    Map<String, List<String>> generateJenisSuratByKategori();
    // Map<String, String> generateJenisSuratByKategori();
    Map<Integer, String> listBentukSurat();
    Map<Integer, String> listBahasa();
    // List<String> getJenisSuratForKategori(String kategori);

    // String generateRequestId(Pengguna dummyPengguna);
    // Pengguna createDummyPengguna(String role);

    // METHOD REQUEST TEMPLATE
    void createRequestTemplate(RequestTemplate requestTemplate);
    List<RequestTemplate> retrieveAllRequest();
    Map<Integer, String> listKategori();

    // PREVIEW TEMPLATE
    List<String> getAllJenisByKategori(String Kategori);
}


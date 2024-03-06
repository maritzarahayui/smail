package propensi.smail.service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import propensi.smail.model.Pengguna;
import propensi.smail.model.RequestSurat;

public interface RequestService {
    RequestSurat createRequestSurat(RequestSurat requestSurat);
    List<RequestSurat> getAllRequests();
    // List<RequestSurat> getRequestsByUser(Pengguna pengguna);
    RequestSurat getRequestSuratById(String requestSuratId);
    // Map<Integer, String> getListUserType();
    int countAllRequests();
    List<String> getAllKategoriSurat();
    List<String> getJenisSuratByKategori(String kategori);
    // String generateRequestId(Pengguna dummyPengguna);
    // Pengguna createDummyPengguna(String role);
    List<String> getAllBentukSurat();
    List<String> getAllBahasaSurat();
}

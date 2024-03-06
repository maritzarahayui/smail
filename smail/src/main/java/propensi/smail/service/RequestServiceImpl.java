package propensi.smail.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import propensi.smail.model.Pengguna;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.Role;
import propensi.smail.repository.RequestSuratDb;

@Service
public class RequestServiceImpl implements RequestService {
    @Autowired
    private RequestSuratDb requestSuratDb;

    @Override
    public RequestSurat createRequestSurat(RequestSurat requestSurat) {
        // requestSurat.setPengaju(pengguna); // sementara gini buat pake dummy, kalo udh nyambung ama pengguna real gimana? 
        return requestSuratDb.save(requestSurat);
    }

    @Override
    public List<RequestSurat> getAllRequests() {
        return requestSuratDb.findAll();
    }

    // @Override
    // public List<RequestSurat> getRequestsByUser(Pengguna pengguna) {
    //     return requestSuratDb.findByPengaju(pengguna);
    // }

    @Override
    public RequestSurat getRequestSuratById(String requestSuratId) {
        Optional<RequestSurat> requestSurat = requestSuratDb.findById(requestSuratId);
        return requestSurat.orElseThrow(() -> new NoSuchElementException("RequestSurat with id: " + requestSuratId + " not found"));
    }

    @Override
    public int countAllRequests() {
        List<RequestSurat> allRequests = getAllRequests();
        return allRequests.size();
    }

    // @Override
    // public String generateRequestId(Pengguna dummyPengguna) {
    //     long totalRequestsLong = requestSuratDb.countByPengajuRole(dummyPengguna.getRole());
    //     int totalRequests = Math.toIntExact(totalRequestsLong);
    //     String prefix = "";

    //     switch (dummyPengguna.getRole()) {
    //         case DOSEN:
    //             prefix = "DOS";
    //             break;
    //         case STAF:
    //             prefix = "STF";
    //             break;
    //         case MAHASISWA:
    //             prefix = "MHS";
    //             break;
    //         default:
    //             break;
    //     }

    //     return prefix + String.format("%03d", totalRequests + 1);
    // }

    // Daftar kategori surat
    private static final List<String> KATEGORI_SURAT = Arrays.asList("SDM", "Keuangan", "Sarana Prasarana");

    // Daftar jenis surat berdasarkan kategori
    private static final Map<String, List<String>> JENIS_SURAT = new HashMap<>();

    static {
        // Kategori SDM
        JENIS_SURAT.put("SDM", Arrays.asList(
            "Surat Kepegawaian",
            "Surat Kenaikan Pangkat / Jabatan",
            "Surat Cuti Pegawai",
            "Surat Pelatihan / Workshop",
            "Surat Karyawan Luar",
            "Surat Izin / Perijinan"
        ));

        // Kategori Keuangan
        JENIS_SURAT.put("Keuangan", Arrays.asList(
            "Surat Tagihan / Faktur",
            "Surat Penerimaan Pembayaran Gaji",
            "Surat Kontrak / Perjanjian Keuangan",
            "Surat Pengajuan Dana"
        ));

        // Kategori Sarana Prasarana
        JENIS_SURAT.put("Sarana Prasarana", Arrays.asList(
            "Surat Peminjaman / Penyewaan",
            "Surat Permohonan Peminjaman Fasilitas"
        ));
    }

    @Override
    public List<String> getAllKategoriSurat() {
        return KATEGORI_SURAT;
    }

    @Override
    public List<String> getJenisSuratByKategori(String kategori) {
        return JENIS_SURAT.getOrDefault(kategori, Collections.emptyList());
    }

    @Override
    public List<String> getAllBentukSurat() {
        return Arrays.asList("Soft Copy", "Hard Copy");
    }

    // Daftar opsi untuk bahasa surat
    @Override
    public List<String> getAllBahasaSurat() {
        return Arrays.asList("Bahasa Indonesia", "Bahasa Inggris");
    }
    
}


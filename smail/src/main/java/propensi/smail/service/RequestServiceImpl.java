package propensi.smail.service;

import org.apache.coyote.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import propensi.smail.model.*;
import propensi.smail.repository.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import propensi.smail.model.user.*;
import propensi.smail.dto.RequestAndFieldDataDTO;

@Service
public class RequestServiceImpl implements RequestService {
    @Autowired
    private RequestSuratDb requestSuratDb;

    @Autowired
    private RequestTemplateDb requestTemplateDb;

    @Autowired
    TemplateSuratDb templateSuratDb;

    @Autowired
    SuratKeluarDb suratKeluarDb;

    @Autowired
    TemplateService templateService;

    @Autowired
    PenggunaService penggunaService;

    @Override
    public void saveOrUpdate(RequestSurat requestSurat) {
        requestSuratDb.save(requestSurat);
    }

    @Override
    public RequestSurat createRequestSurat(RequestSurat requestSurat, RequestAndFieldDataDTO requestDTO) {
        try {
            System.out.println("msk service");

            requestSurat.setBentukSurat(requestDTO.getBentukSurat());
            requestSurat.setBahasa(requestDTO.getBahasa());
            requestSurat.setKategori(requestDTO.getKategori());
            requestSurat.setJenisSurat(requestDTO.getJenisSurat());
            requestSurat.setKeperluan(requestDTO.getKeperluan());
            requestSurat.setStatus(1); // Diajukan
            requestSurat.setId(generateRequestId(requestSurat.getPengaju()));
            requestSurat.setListFieldData(requestDTO.getListFieldData());

            for (FieldData fieldData : requestDTO.getListFieldData()) {
                fieldData.setRequestSurat(requestSurat);
            } 

            System.out.println("RequestTemplate saved successfully");
            return requestSuratDb.save(requestSurat);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error saving RequestTemplate");
            return null;
        }
        
    }

    @Override
    public List<RequestSurat> getAllRequestsSurat() {
        return requestSuratDb.findAll();
    }

    @Override
    public List<RequestSurat> getAllSubmitedRequestsSurat() {
        return requestSuratDb.findByStatus(1);
    }

    @Override
    public List<RequestSurat> getAllCanceledRequestsSurat() {
        return requestSuratDb.findByStatus(2);
    }

    @Override
    public List<RequestSurat> getAllRejectedRequestsSurat() {
        return requestSuratDb.findByStatus(3);
    }

    @Override
    public List<RequestSurat> getAllOnProcessRequestsSurat() {
        return requestSuratDb.findByStatus(4);
    }

    @Override
    public List<RequestSurat> getAllFinishedRequestsSurat() {
        return requestSuratDb.findByStatus(5);
    }

    @Override
    public RequestSurat getRequestSuratById(String requestSuratId) {
        Optional<RequestSurat> requestSurat = requestSuratDb.findById(requestSuratId);
        return requestSurat.orElseThrow(() -> new NoSuchElementException("RequestSurat with id: " + requestSuratId + " not found"));
    }

    @Override
    public RequestSurat findRequestById(String id) {
        return requestSuratDb.findByIdContainingIgnoreCase(id);
    }

    @Override
    public List<RequestSurat> getRequestByJenisSurat(String jenisSurat) {
        return requestSuratDb.findByJenisSuratContainingIgnoreCase(jenisSurat);
    }

    @Override
    public List<RequestSurat> getRequestByTanggalPengajuan(Date tanggalPengajuan) {
        return requestSuratDb.findByTanggalPengajuan(tanggalPengajuan);
    }

    @Override
    public List<RequestSurat> getRequestByTanggalDibatalkan(Date tanggalDibatalkan) {
        return requestSuratDb.findByTanggalDibatalkan(tanggalDibatalkan);
    }

    @Override
    public List<RequestSurat> getRequestByTanggalPengajuanOrTanggalDibatalkan(Date tanggalPengajuan, Date tanggalDibatalkan) {
        return requestSuratDb.findByTanggalPengajuanOrTanggalDibatalkan(tanggalPengajuan, tanggalDibatalkan);
    }

    @Override
    public List<RequestSurat> getRequestByTanggalPenolakan(Date tanggalPenolakan) {
        return requestSuratDb.findByTanggalPenolakan(tanggalPenolakan);
    }

    @Override
    public List<RequestSurat> getRequestByTanggalPengajuanOrTanggalPenolakan(Date tanggalPengajuan, Date tanggalPenolakan) {
        return requestSuratDb.findByTanggalPengajuanOrTanggalPenolakan(tanggalPengajuan, tanggalPenolakan);
    }

    @Override
    public List<RequestSurat> getRequestByTanggalSelesai(Date tanggalSelesai) {
        return requestSuratDb.findByTanggalSelesai(tanggalSelesai);
    }

    @Override
    public List<RequestSurat> getRequestByTanggalPengajuanOrTanggalSelesai(Date tanggalPengajuan, Date tanggalSelesai) {
        return requestSuratDb.findByTanggalPengajuanOrTanggalSelesai(tanggalPengajuan, tanggalSelesai);
    }

    // @Override
    // public RequestSurat batalkanRequestSurat(String requestSuratId) {
    //     RequestSurat requestSurat = getRequestSuratById(requestSuratId);
    //     requestSurat.setStatus(2); // Dibatalkan
    //     return requestSuratDb.save(requestSurat);
    // }

    @Override
    public RequestSurat batalkanRequestSurat(String requestSuratId, String alasanPembatalan) {
        RequestSurat requestSurat = getRequestSuratById(requestSuratId);
        requestSurat.setStatus(2); // Dibatalkan
        requestSurat.setAlasanPembatalan(alasanPembatalan);
        requestSurat.setTanggalDibatalkan(new Date());
        return requestSuratDb.save(requestSurat);
    }

    @Override
    public int countAllRequests() {
        List<RequestSurat> allRequests = getAllRequestsSurat();
        return allRequests.size();
    }

    @Override
    public String generateRequestId(Pengguna pengaju) {
        Long totalRequestsBy = (long) 0;
        String prefix = "";

        if (pengaju instanceof Dosen) {
            totalRequestsBy = requestSuratDb.countRequestByDosen();
            prefix = "DOS";
        } else if (pengaju instanceof Staf) {
            totalRequestsBy = requestSuratDb.countRequestByStaf();
            prefix = "STF";
        } else if (pengaju instanceof Mahasiswa) {
            totalRequestsBy = requestSuratDb.countRequestByMahasiswa();
            prefix = "MHS";
        }

        return prefix + String.format("%03d", totalRequestsBy + 1);
    }

    @Override
    public Map<String, List<String>> generateJenisSuratByKategoriAndRole(String tipePengaju) {
        List<TemplateSurat> templateSuratList = templateSuratDb.findAll();
        Map<String, List<String>> kategoriJenisMap = new HashMap<>();

        // Loop through the templateSuratList
        for (TemplateSurat template : templateSuratList) {

            if (template.getListPengguna().contains(tipePengaju)) {
                String kategori = template.getKategori();
                String jenis = template.getNamaTemplate();

                // If the category already exists in the map, add the type to its list
                if (kategoriJenisMap.containsKey(kategori)) {
                    kategoriJenisMap.get(kategori).add(jenis);
                } else { // Otherwise, create a new list for the category and add the type to it
                    List<String> jenisList = new ArrayList<>();
                    jenisList.add(jenis);
                    kategoriJenisMap.put(kategori, jenisList);
                }
            }
            
        }

        return kategoriJenisMap;
    }
    
    @Override
    public Map<Integer, String> listBahasa(){
        Map<Integer, String> bahasa = new HashMap<>();

        bahasa.put(1, "Indonesia (IND)");
        bahasa.put(2, "Inggris (EN)");

        return bahasa;
    }

    @Override
    public Map<Integer, String> listBentukSurat(){
        Map<Integer, String> bentuk = new HashMap<>();

        bentuk.put(1, "Soft Copy");
        bentuk.put(2, "Hard Copy");

        return bentuk;
    }

    @Override
    public List<RequestSurat> searchRequests(String keyword, int status) {

        List<RequestSurat> suratList = requestSuratDb.findByKeyword(keyword);
        List<RequestSurat> resultSurat = new ArrayList<>();

        for (RequestSurat rs : suratList) {
            if (status == 1) {
                if (rs.getStatus() == 1) {
                    resultSurat.add(rs);
                }
            } if (status == 2) {
                if (rs.getStatus() == 2) {
                    resultSurat.add(rs);
                }
            } if (status == 3) {
                if (rs.getStatus() == 3) {
                    resultSurat.add(rs);
                }
            } if (status == 4) {
                if (rs.getStatus() == 4) {
                    resultSurat.add(rs);
                }
            } if (status == 5) {
                if (rs.getStatus() == 5) {
                    resultSurat.add(rs);
                }
            }
        }

        return resultSurat;
    }

    @Override
    public List<RequestSurat> getAllSubmittedRequestsSuratByPengaju(String penggunaId) {
        return requestSuratDb.findByStatusAndPengajuId(1, penggunaId);
    }

    @Override
    public List<RequestSurat> getAllCancelledRequestsSuratByPengaju(String penggunaId) {
        return requestSuratDb.findByStatusAndPengajuId(2, penggunaId);
    }

    @Override
    public List<RequestSurat> getAllRejectedRequestsSuratByPengaju(String penggunaId) {
        return requestSuratDb.findByStatusAndPengajuId(3, penggunaId);
    }

    @Override
    public List<RequestSurat> getAllOnProcessRequestsSuratByPengaju(String penggunaId) {
        return requestSuratDb.findByStatusAndPengajuId(4, penggunaId);
    }

    @Override
    public List<RequestSurat> getAllFinishedRequestsSuratByPengaju(String penggunaId) {
        return requestSuratDb.findByStatusAndPengajuId(5, penggunaId);
    }

    @Override
    public List<RequestSurat> getAllRequestSuratByPenandatanganId(String penandatanganId) {
        // Retrieve all SuratKeluar objects associated with the specified penandatanganId
        List<SuratKeluar> suratKeluarList = suratKeluarDb.findByPenandatanganId(penandatanganId);

        // Create a list to store the associated RequestSurat objects
        List<RequestSurat> requestSuratList = new ArrayList<>();

        // Iterate over the SuratKeluar objects
        for (SuratKeluar suratKeluar : suratKeluarList) {
            // Retrieve the associated RequestSurat object
            RequestSurat requestSurat = suratKeluar.getRequestSurat();

            // Add the retrieved RequestSurat object to the list
            requestSuratList.add(requestSurat);
        }

        // Return the list of associated RequestSurat objects
        return requestSuratList;
    }

    // ------------------REQUEST TEMPLATE----------------
    @Override
    public void createRequestTemplate(RequestTemplate requestTemplate, RequestAndFieldDataDTO requestDTO){
        try {
            requestTemplate.setBahasa(requestDTO.getBahasa());
            requestTemplate.setKategori(requestDTO.getKategori());
            requestTemplate.setStatus(1); // 1 --> REQUESTED
            requestTemplate.setKeperluan(requestDTO.getKeperluan()); // 0 --> REQUESTED
            requestTemplate.setId(generateRequestId(requestTemplate.getPengaju()));
            requestTemplate.setListFieldData(requestDTO.getListFieldData());

            for (FieldData fieldData : requestDTO.getListFieldData()) {
                fieldData.setRequestTemplate(requestTemplate);
            } 

            requestTemplateDb.save(requestTemplate);

            System.out.println("RequestTemplate saved successfully");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error saving RequestTemplate");
        }
    }

    @Override
    public List<RequestTemplate> retrieveAllRequest(){
        return requestTemplateDb.findAll();
    }

    @Override
    public Map<Integer, String> listKategori(){
        Map<Integer, String> kategori = new HashMap<>();

        kategori.put(1, "LEGAL");
        kategori.put(2, "SDM");
        kategori.put(3, "KEUANGAN");
        kategori.put(4, "SARANA");
        kategori.put(5, "KEMAHASISWAAN");
        return kategori;
    }

    // ------PREVIEW-----
    @Override
    public List<String> getAllJenisByKategori(String kategori) {
        return templateSuratDb.findNamaTemplateByKategori(kategori);
    }

    @Override
    public List<RequestSurat> getBySearchAndStatusAndPengaju(int status, String search, String pengaju) {
        return requestSuratDb.findBySearchAndStatusAndPengajuId(search, status, pengaju);
    }


    /* DASHBOARD */
    @Override
    public Integer countDurasi(RequestSurat requestSurat) {
        long tanggalPengajuan = requestSurat.getTanggalPengajuan().getTime();        
        long tanggalSelesai = requestSurat.getTanggalSelesai().getTime();

        long durationInMillis = tanggalSelesai - tanggalPengajuan;
        long durationInDays = durationInMillis / (1000 * 60 * 60 * 24);

        // Print the duration
        System.out.println("awal: " + requestSurat.getTanggalPengajuan());
        System.out.println("akhir " + requestSurat.getTanggalSelesai());
        System.out.println("Duration in days: " + durationInDays);

        return ((int)durationInDays);
    }

    @Override
    public Integer countAveragePerforma(List<RequestSurat> listRequestSurat) {
        int total = 0;
        int counterRequest = 0;

        for (RequestSurat request : listRequestSurat) {
            if (request.getTanggalSelesai() != null) {
                total += countDurasi(request);
                counterRequest++;
            }
        }

        System.out.println(listRequestSurat.toString());

        return counterRequest == 0? 0 : (int) Math.ceil(total/counterRequest);
    }

    @Override
    public Map<String, Integer> getPerformaRequestSurat() {
        LocalDate now = LocalDate.now();
        Map<String, Integer> mapPerBulan = new LinkedHashMap<String, Integer>();
        String[] indonesianMonths = new String[] {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des"};
        
        int value = 0;
        int counter = 0;
        List<RequestSurat> allRequestSuratMonthly;

        for (String bulan : indonesianMonths) {
            counter++;
            allRequestSuratMonthly = requestSuratDb.findByTanggalPengajuanMonthly(counter, now.getYear());

            System.out.println("WOIIIII MONTHLY" + counter + now.getYear());
            System.out.println("WOIIIII MONTHLY" + allRequestSuratMonthly.toString());
            value = countAveragePerforma(allRequestSuratMonthly);
            mapPerBulan.put(bulan, value);
        }

        System.out.println("MAPPPPP PER BULAN" + mapPerBulan.toString());
        return mapPerBulan;        
    }

    @Override
    public Map<String, Long> getJumlahRequestPerMinggu() {
        List<RequestSurat> allRequestSurat = requestSuratDb.findAll();
    
        Map<String, Long> jumlahRequestPerMinggu = new HashMap<>();

        if (allRequestSurat == null || allRequestSurat.isEmpty()) {
            jumlahRequestPerMinggu.put("", 0L);
        } else {
            for (RequestSurat requestSurat : allRequestSurat) {
                int weekOfMonth = getWeekOfMonth(requestSurat.getTanggalPengajuan());
                String key = "Minggu ke-" + weekOfMonth;

                jumlahRequestPerMinggu.put(key, jumlahRequestPerMinggu.getOrDefault(key, 0L) + 1);
            }
        }

        List<Map.Entry<String, Long>> sortedList = new ArrayList<>(jumlahRequestPerMinggu.entrySet());
        Collections.sort(sortedList, Comparator.comparing(Map.Entry::getKey));

        Map<String, Long> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : sortedList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
    
        return sortedMap;
    }

    @Override
    public Map<String, Long> getJumlahRequestPerMonth() {
        List<RequestSurat> allRequestSurat = requestSuratDb.findAll();
    
        Map<String, Long> jumlahRequestPerMonth = new HashMap<>();

        if (allRequestSurat == null || allRequestSurat.isEmpty()) {
            jumlahRequestPerMonth.put("", 0L);
        } else {
            for (RequestSurat requestSurat : allRequestSurat) {
                String monthName = getMonthName(requestSurat.getTanggalPengajuan().getMonth() + 1); // Bulan dimulai dari 0

                jumlahRequestPerMonth.put(monthName, jumlahRequestPerMonth.getOrDefault(monthName, 0L) + 1);
            }
        }
    
        return jumlahRequestPerMonth;
    }

    @Override
    public Map<String, Long> getJumlahRequestPerYear() {
        List<RequestSurat> allRequestSurat = requestSuratDb.findAll();
    
        Map<String, Long> jumlahRequestPerYear = new HashMap<>();
    
        int tahunSaatIni = LocalDate.now().getYear();

        if (allRequestSurat == null || allRequestSurat.isEmpty()) {
            jumlahRequestPerYear.put("", 0L);
        } else {
            for (RequestSurat requestSurat : allRequestSurat) {
                int year = requestSurat.getTanggalPengajuan().getYear() + 1900;

                if (year == tahunSaatIni) {
                    jumlahRequestPerYear.put(String.valueOf(year), jumlahRequestPerYear.getOrDefault(String.valueOf(tahunSaatIni), 0L) + 1);
                } else {
                    jumlahRequestPerYear.put(String.valueOf(year), 1L);
                }
            }
        }
        
        return jumlahRequestPerYear;
    }

    // Method untuk mendapatkan nama bulan dari nomor bulan
    private String getMonthName(int monthNumber) {
        String[] months = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        };
        return months[monthNumber - 1]; // Kurangi 1 karena array dimulai dari indeks 0
    }

    // Method untuk mendapatkan minggu dalam bulan dari tanggal
    private int getWeekOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.WEEK_OF_MONTH);
    }

    @Override
    public Map<String, Long> getJumlahRequestByKategori() {
        List<RequestSurat> allRequestSurat = requestSuratDb.findAll();

        Map<String, Long> jumlahRequestByKategori = new HashMap<>();

        if (allRequestSurat == null || allRequestSurat.isEmpty()) {
            jumlahRequestByKategori.put("", 0L);
        } else {
            for (RequestSurat requestSurat : allRequestSurat) {
                String kategori = requestSurat.getKategori();

                jumlahRequestByKategori.put(kategori, jumlahRequestByKategori.getOrDefault(kategori, 0L) + 1);
            }
        }
    
        return jumlahRequestByKategori;
    }

    @Override
    public Map<String, Long> getJumlahRequestByRole() {
        List<RequestSurat> allRequestSurat = requestSuratDb.findAll();
    
        Map<String, Long> jumlahRequestByRole = new HashMap<>();

        if (allRequestSurat == null || allRequestSurat.isEmpty()) {
            jumlahRequestByRole.put("", 0L);
        } else {
            for (RequestSurat requestSurat : allRequestSurat) {
                Pengguna pengguna = requestSurat.getPengaju();
                String role = penggunaService.getRole(pengguna);

                jumlahRequestByRole.put(role, jumlahRequestByRole.getOrDefault(role, 0L) + 1);
            }
        }
    
        return jumlahRequestByRole;
    }

    @Override
    public String getTopRequester() {
        return requestSuratDb.findTopRequester();
    }
    
}


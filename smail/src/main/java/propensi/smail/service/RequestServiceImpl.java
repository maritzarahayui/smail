package propensi.smail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.apache.coyote.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import propensi.smail.model.*;
import propensi.smail.repository.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import propensi.smail.model.user.*;
import propensi.smail.dto.RequestAndFieldDataDTO;

@Service
@Transactional
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

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendEmailRejection(String to, String subject, String body, RequestSurat requestSurat) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        Date tanggalDibuat = requestSurat.getTanggalPengajuan();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));

         body = String.format("Yth Bapak/Ibu %s,\n\n" // Include the name of the requester
                        + "Terima kasih atas permintaan surat dengan jenis %s (%s) yang telah diajukan kepada kami pada tanggal %s untuk keperluan: %s.\n\n" // Include the date of the request and purpose
                        + "Berdasarkan peninjauan admin, kami tidak dapat melanjutkan permintaan surat dengan ID %s karena alasan berikut:\n\n" // Include the ID
                        + "- %s\n\n" // Include the rejection reason
                        + "Mohon untuk dapat melakukan evaluasi berdasarkan informasi tersebut sebelum mengajukan permintaan baru. Untuk melakukan pengajuan permintaan baru atau pertanyaan terkait pengajuan, silakan kunjungi SMAIL Institut Tazkia melalui tautan berikut: https://smail-rtx.up.railway.app/. Terima kasih atas pengertiannya.\n\n"
                        + "Salam,\n"
                        + "Yayasan Tazkia\n"
                        + "Jl. Ir. H. Djuanda No. 78, Bogor, Jawa Barat 16122\n",
                requestSurat.getPengaju().getNama(), // Retrieves the name from requestTemplate
                requestSurat.getJenisSurat(), // Retrieves the type of the request from requestTemplate
                 requestSurat.getKategori(),
                dateFormat.format(tanggalDibuat), // Retrieves the date of the request from requestTemplate
                requestSurat.getKeperluan(), // Retrieves the purpose of the request from requestTemplate
                requestSurat.getId(), // Retrieves the ID from requestTemplate
                requestSurat.getAlasanPenolakan()); // Retrieves the rejection reason from requestTemplate

        helper.setTo(to);
        helper.setSubject("[DITOLAK] Permintaan Surat dengan ID " + requestSurat.getId());
        helper.setText(body, false);
        helper.setFrom("instituttazkia.adm@gmail.com");

        mailSender.send(message);
    }

    @Async
    public void sendEmailFinished(String to, String subject, String body, RequestSurat requestSurat, SuratKeluar suratKeluar) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String tanggalDibuat = dateFormat.format(suratKeluar.getTanggalDibuat());
        body = String.format("Yth Bapak/Ibu %s,\n\n"
                        + "Kami dengan ini memberitahukan bahwa permintaan surat dengan ID %s telah selesai diproses dan dapat diunduh.\n\n"
                        + "Berikut adalah detail mengenai surat tersebut:\n\n"
                        + "Nomor Surat  : %s\n"
                        + "Jenis Surat   : %s\n"
                        + "Kategori       : %s\n"
                        + "Bahasa        : %s\n"
                        + "Keperluan     : %s\n\n"
                        + "Anda dapat mengunduh surat tersebut melalui file yang terlampir. "
                        + "Jika Anda melakukan permintaan untuk surat hardcopy, Anda dapat mengambil surat Anda di Ruang Sekretariat Institut Tazkia di jam kerja. Untuk informasi lebih lanjut, Anda dapat mengakses surat pada SMAIL Institut Tazkia melalui tautan berikut: https://smail-rtx.up.railway.app/.\n\n\n"
                        + "Salam,\n"
                        + "Yayasan Tazkia\n"
                        + "Jl. Ir. H. Djuanda No. 78, Bogor, Jawa Barat 16122\n",
                requestSurat.getPengaju().getNama(),
                requestSurat.getId(), // Retrieves the ID of the request from suratMasuk
                suratKeluar.getNomorArsip(),
                requestSurat.getJenisSurat(), // Retrieves the type of the request from suratMasuk
                requestSurat.getKategori(), // Retrieves the category of the request from suratMasuk
                requestSurat.getBahasa(), // Retrieves the language of the request from suratMasuk
                requestSurat.getKeperluan()); // Retrieves the purpose of the request from suratMasuk

        helper.setTo(to);
        helper.setSubject("[SELESAI] Permintaan Surat dengan ID " + requestSurat.getId());
        helper.setText(body, false);
        helper.setFrom("instituttazkia.adm@gmail.com");
        helper.addAttachment(suratKeluar.getFileName(), new ByteArrayDataSource(suratKeluar.getFile(), "application/pdf")); // Specify the content type for the attachment

        mailSender.send(message);
    }

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
    @Transactional
    public RequestTemplate store(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            RequestTemplate requestTemplate = new RequestTemplate();
            requestTemplate.setFile(file.getBytes());
            requestTemplate.setFileName(fileName);
            return requestTemplateDb.save(requestTemplate);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + fileName, e);
        }
    }

    @Override
    public RequestTemplate getFile(String id) {
        Optional<RequestTemplate> optionalRequestSurat = Optional.ofNullable(getRequestTemplateById(id));
        if (optionalRequestSurat.isPresent()) {
            return optionalRequestSurat.get();
        } else {
            return null;
        }
    }

    @Override
    public RequestTemplate getRequestTemplateById(String requestSuratId) {
        Optional<RequestTemplate> requestTemplate = requestTemplateDb.findById(requestSuratId);
        return requestTemplate.orElseThrow(() -> new NoSuchElementException("RequestTemplate with id: " + requestSuratId + " not found"));
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

            if (template.getListPengguna().contains(tipePengaju) && template.isActive()) {
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
            if (suratKeluar.getRequestSurat() != null) {
                // Retrieve the associated RequestSurat object
                RequestSurat requestSurat = suratKeluar.getRequestSurat();

                // Add the retrieved RequestSurat object to the list
                requestSuratList.add(requestSurat);
            }
        }

        // Return the list of associated RequestSurat objects
        return requestSuratList;
    }

    @Override
    public List<RequestSurat> searchRequestsTTD(String keyword, String penandatanganId) {
        List<RequestSurat> requestSurats = getAllRequestSuratByPenandatanganId(penandatanganId);

        // Filter the list by keyword
        return requestSurats.stream()
                .filter(requestSurat -> requestSurat.getId().toLowerCase().contains(keyword.toLowerCase()) ||
                        requestSurat.getJenisSurat().toLowerCase().contains(keyword.toLowerCase()) ||
                        requestSurat.getPengaju().getNama().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
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
    public Map<String, Map<String, Long>> getJumlahRequestPerMinggu() {
        List<RequestSurat> allRequestSurat = requestSuratDb.findAll();

        Map<String, Map<String, Long>> jumlahRequestPerMinggu = new HashMap<>();

        if (allRequestSurat != null && !allRequestSurat.isEmpty()) {
            for (RequestSurat requestSurat : allRequestSurat) {
                LocalDate tanggalPengajuan = requestSurat.getTanggalPengajuan().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                int year = tanggalPengajuan.getYear();
                int month = tanggalPengajuan.getMonthValue();
                int week = tanggalPengajuan.get(WeekFields.of(Locale.getDefault()).weekOfMonth());

                String key = year + "-" + month;
                Map<String, Long> mingguTahunBulanIni = jumlahRequestPerMinggu.getOrDefault(key, new HashMap<>());

                Long jumlahPermintaanMingguIni = mingguTahunBulanIni.getOrDefault("Minggu ke-" + week, 0L);
                mingguTahunBulanIni.put("Minggu ke-" + week, jumlahPermintaanMingguIni + 1);

                jumlahRequestPerMinggu.put(key, mingguTahunBulanIni);
            }
        }

        jumlahRequestPerMinggu.forEach((key, value) -> {
            Map<String, Long> sortedValue = value.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            jumlahRequestPerMinggu.put(key, sortedValue);
        });

        System.out.println("jumlahRequestPerMinggu: " + jumlahRequestPerMinggu.toString());
        return jumlahRequestPerMinggu;
    }

    @Override
    public String getCurrentYearMonth() {
        LocalDate currentDate = LocalDate.now();
        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();
        return year + "-" + month;
    }

    @Override
    public Map<String, Long> getJumlahRequestPerMonth() {
        List<RequestSurat> allRequestSurat = requestSuratDb.findAll();
    
        Map<String, Long> jumlahRequestPerMonth = new HashMap<>();

        if (allRequestSurat == null || allRequestSurat.isEmpty()) {
            jumlahRequestPerMonth.put("", 0L);
        } else {
            for (RequestSurat requestSurat : allRequestSurat) {
                if (requestSurat.getTanggalPengajuan().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() == LocalDate.now().getYear() &&
                    requestSurat.getTanggalPengajuan().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue() == LocalDate.now().getMonthValue()) {
                    String monthName = getMonthName(requestSurat.getTanggalPengajuan().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
                    jumlahRequestPerMonth.put(monthName, jumlahRequestPerMonth.getOrDefault(monthName, 0L) + 1);
                }
            }
        }
    
        System.out.println("jumlahRequestPerMonth" + jumlahRequestPerMonth.toString());
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
                }
            }
        }
 
        System.out.println("jumlahRequestPerYear" + jumlahRequestPerYear.toString());
        return jumlahRequestPerYear;
    }

    @Override
    public Map<String, Map<String, Long>> getJumlahRequestPerYearAndMonth() {
        List<RequestSurat> allRequestSurat = requestSuratDb.findAll();

        Map<String, Map<String, Long>> jumlahRequestPerYearAndMonth = new HashMap<>();

        if (allRequestSurat != null && !allRequestSurat.isEmpty()) {
            for (RequestSurat requestSurat : allRequestSurat) {
                int year = requestSurat.getTanggalPengajuan().getYear() + 1900;
                String monthName = getMonthName(requestSurat.getTanggalPengajuan().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());

                // Ambil map bulan untuk tahun saat ini atau buat baru jika belum ada
                Map<String, Long> bulanTahunIni = jumlahRequestPerYearAndMonth.getOrDefault(String.valueOf(year), new HashMap<>());

                // Dapatkan jumlah permintaan untuk bulan ini atau 0 jika belum ada
                Long jumlahPermintaanBulanIni = bulanTahunIni.getOrDefault(monthName, 0L);

                // Tambahkan jumlah permintaan untuk bulan ini ke dalam map bulan
                bulanTahunIni.put(monthName, jumlahPermintaanBulanIni + 1);

                // Set map bulan untuk tahun saat ini kembali ke dalam map utama
                jumlahRequestPerYearAndMonth.put(String.valueOf(year), bulanTahunIni);
            }
        }

        System.out.println("jumlahRequestPerYearAndMonth: " + jumlahRequestPerYearAndMonth.toString());
        return jumlahRequestPerYearAndMonth;
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
    

    // EMI SPRINT 3
    @Override
    public Map<String, Long> getJumlahRequestPerMingguByUser(Pengguna pengguna) {
        List<RequestSurat> allRequestSurat = requestSuratDb.findByPengaju(pengguna);
        Map<String, Long> jumlahRequestPerMinggu = new HashMap<>();

        for (RequestSurat requestSurat : allRequestSurat) {
            int weekOfMonth = getWeekOfMonth(requestSurat.getTanggalPengajuan());
            String key = "Minggu ke-" + weekOfMonth;
            jumlahRequestPerMinggu.put(key, jumlahRequestPerMinggu.getOrDefault(key, 0L) + 1);
        }
        // Sort map berdasarkan kunci (minggu)
        List<Map.Entry<String, Long>> sortedList = new ArrayList<>(jumlahRequestPerMinggu.entrySet());
        Collections.sort(sortedList, Comparator.comparing(Map.Entry::getKey));
    
        // Buat map hasil yang sudah terurut
        Map<String, Long> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : sortedList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    @Override
    public Map<String, Long> getJumlahRequestPerMonthByUser(Pengguna pengguna) {
        List<RequestSurat> allRequestSurat = requestSuratDb.findByPengaju(pengguna);
        Map<String, Long> jumlahRequestPerMinggu = new HashMap<>();
        for (RequestSurat requestSurat : allRequestSurat) {
            String monthName = getMonthName(requestSurat.getTanggalPengajuan().getMonth() + 1); // Bulan dimulai dari 0
            jumlahRequestPerMinggu.put(monthName, jumlahRequestPerMinggu.getOrDefault(monthName, 0L) + 1);
        }
        return jumlahRequestPerMinggu;
    }

    @Override
    public Map<String, Long> getJumlahRequestPerYearByUser(Pengguna pengguna) {
        List<RequestSurat> allRequestSurat = requestSuratDb.findByPengaju(pengguna);
        Map<String, Long> jumlahRequestPerMonth = new HashMap<>();
        int tahunSaatIni = LocalDate.now().getYear();
        
        for (RequestSurat requestSurat : allRequestSurat) {
            int year = requestSurat.getTanggalPengajuan().getYear() + 1900;
            if (year == tahunSaatIni) {
                jumlahRequestPerMonth.put(String.valueOf(year), jumlahRequestPerMonth.getOrDefault(String.valueOf(tahunSaatIni), 0L) + 1);
            } else {
                jumlahRequestPerMonth.put(String.valueOf(year), 1L);
            }
        }
        return jumlahRequestPerMonth;
    }
    
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
    public Map<String, Long> getJumlahRequestByStatus(Pengguna pengguna) {
        Map<String, Long> jumlahRequestByStatus = new HashMap<>();
        jumlahRequestByStatus.put("Diajukan", requestSuratDb.countByPengajuAndStatus(pengguna, 1));
        jumlahRequestByStatus.put("Dibatalkan", requestSuratDb.countByPengajuAndStatus(pengguna, 2));
        jumlahRequestByStatus.put("Ditolak", requestSuratDb.countByPengajuAndStatus(pengguna, 3));
        jumlahRequestByStatus.put("Diproses", requestSuratDb.countByPengajuAndStatus(pengguna, 4));
        jumlahRequestByStatus.put("Selesai", requestSuratDb.countByPengajuAndStatus(pengguna, 5));
        return jumlahRequestByStatus;
    }

    // @Override
    // public long countRequestsSignedByDosen() {
    //     return requestSuratDb.countRequestByStatusAndRole(5, Dosen.class);
    // }

    @Override
    public Map<String, Long> getCountOfRequestByCategory(Pengguna pengguna) {
        Map<String, Long> countByCategory = new HashMap<>();
        List<RequestSurat> requests = requestSuratDb.findByPengaju(pengguna);

        for (RequestSurat request : requests) {
            String kategori = request.getKategori();
            countByCategory.put(kategori, countByCategory.getOrDefault(kategori, 0L) + 1);
        }
        return countByCategory;
    }

    @Override
    public Map<String, Long> getCountOfRequestByJenis(Pengguna pengguna) {
        Map<String, Long> countByJenis = new HashMap<>();
        List<RequestSurat> requests = requestSuratDb.findByPengaju(pengguna);

        for (RequestSurat request : requests) {
            String jenis = request.getJenisSurat();
            countByJenis.put(jenis, countByJenis.getOrDefault(jenis, 0L) + 1);
        }
        return countByJenis;
    }
}



package propensi.smail.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.SuratKeluar;
import propensi.smail.model.TemplateSurat;
import propensi.smail.model.user.Pengguna;
import propensi.smail.repository.RequestSuratDb;
import propensi.smail.repository.SuratKeluarDb;
import propensi.smail.repository.TemplateSuratDb;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class SuratKeluarServiceImpl implements SuratKeluarService {
    @Autowired
    SuratKeluarDb suratKeluarDb;

    @Autowired
    RequestSuratDb requestSuratDb;

    @Autowired
    private TemplateSuratDb templateSuratDb;

    @Override
    public List<SuratKeluar> getAllSuratKeluar() {
        return suratKeluarDb.findAll();
    }
    
    @Override
    @Transactional
    public SuratKeluar storeTtd(RequestSurat requestSurat, MultipartFile file, String kategori, String jenisSurat, List<Pengguna> penandatangans) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            SuratKeluar suratKeluar = new SuratKeluar();

            suratKeluar.setRequestSurat(requestSurat); // Set the corresponding RequestSurat
            suratKeluar.setNomorArsip(generateId(kategori));
            suratKeluar.setKategori(kategori);
            suratKeluar.setJenisSurat(jenisSurat);
            suratKeluar.setTanggalDibuat(new Date());
            suratKeluar.setFileName(fileName);
            suratKeluar.setFile(file.getBytes());

            suratKeluar.setPengaju(requestSurat.getPengaju());

            ArrayList<Pengguna> pList = new ArrayList<>();
            suratKeluar.setPenandatangan(pList);

            for (Pengguna p : penandatangans) {
                suratKeluar.getPenandatangan().add(p);
            }

            suratKeluar.setCurrentPenandatangan(penandatangans.get(0));

            return suratKeluarDb.save(suratKeluar);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + fileName, e);
        }
    }

    @Override
    public String generateId(String kategori) {
        Map<String, String> kategoriMap = Map.of(
                "LEGAL", "LGL",
                "SDM", "SDM",
                "KEUANGAN", "KEU",
                "SARANA", "SAR",
                "KEMAHASISWAAN", "KMH"
        );

        String abbreviation = kategoriMap.get(kategori.toUpperCase());

        if (abbreviation != null) {
            long count = suratKeluarDb.countByKategori(kategori);

            String idSuffix = String.format("%05d", count + 1);
            return "IN" + "-" + abbreviation + "-" + idSuffix;
        } else {
            throw new IllegalArgumentException("Invalid kategori: " + kategori);
        }
    }

    @Override
    public SuratKeluar getFileTtd(String id) {
        Optional<SuratKeluar> optionalSuratKeluar = Optional.ofNullable(findSuratKeluarByRequestID(id));
        if (optionalSuratKeluar.isPresent()) {
            return optionalSuratKeluar.get();
        } else {
            return null;
        }
    }

    @Override
    public SuratKeluar findSuratKeluarByRequestID(String id) {
        List<SuratKeluar> suratKeluars = suratKeluarDb.findAll();

        for (SuratKeluar suratKeluar : suratKeluars) {
            if (suratKeluar.getRequestSurat() != null) {
                if (suratKeluar.getRequestSurat().getId().equals(id)) {
                    return suratKeluar;
                }
            }
        }
        return null;
    }

    @Override
    public SuratKeluar findSuratKeluarByNomorArsip(String nomorArsip) {
        return suratKeluarDb.findByNomorArsipContainingIgnoreCase(nomorArsip);
    }
    
    @Transactional
    public void update(SuratKeluar suratKeluar) {
        suratKeluarDb.save(suratKeluar);
    }

    @Override
    public void updateSuratKeluarFile(String id, MultipartFile file) {
        // Retrieve the SuratKeluar object by ID
        Optional<RequestSurat> requestSurat = requestSuratDb.findById(id);

        SuratKeluar suratKeluar = suratKeluarDb.findByRequestSurat(requestSurat);

        try {
            System.out.println("masuk serv");

            // Check if a new file is uploaded
            if (file != null && !file.isEmpty()) {
                // Convert the file to byte array
                byte[] fileBytes = file.getBytes();

                // Set the file and file name
                suratKeluar.setFile(fileBytes);
                suratKeluar.setFileName(file.getOriginalFilename());

                Pengguna current = findNextSignatory(suratKeluar);

                suratKeluar.setCurrentPenandatangan(current);

                if (current == null) {
                    suratKeluar.getRequestSurat().setStatus(5);
                    suratKeluar.getRequestSurat().setTanggalSelesai(new Date());
                }

                suratKeluarDb.save(suratKeluar);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to update SuratKeluar file: " + e.getMessage());
        }
    }



    private Pengguna findNextSignatory(SuratKeluar suratKeluar) {
        List<Pengguna> penandatangans = suratKeluar.getPenandatangan();
        Pengguna currentSignatory = suratKeluar.getCurrentPenandatangan();

        int currentIndex = penandatangans.indexOf(currentSignatory);
        int nextIndex = currentIndex + 1;

        if (nextIndex < penandatangans.size()) {
            return penandatangans.get(nextIndex);
        } else {
            // All signatories have signed
            return null;
        }
    }

    @Override
    public SuratKeluar storeInput(MultipartFile file, String kategori, String perihal, String penerimaEksternal) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        //debug
        System.out.println("File Name: " + fileName);
        System.out.println(kategori);
        System.out.println(penerimaEksternal);

        try {
            SuratKeluar suratKeluar = new SuratKeluar();
                suratKeluar.setNomorArsip(generateId(kategori));
                suratKeluar.setFile(file.getBytes());
                suratKeluar.setKategori(kategori);
                suratKeluar.setPerihal(perihal);
                suratKeluar.setTanggalDibuat(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
                suratKeluar.setPenerimaEksternal(penerimaEksternal);
                suratKeluar.setFileName(fileName);
                return suratKeluarDb.save(suratKeluar);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + fileName, e);
        }
    }

    @Override
    public Stream<SuratKeluar> getAllFiles() {
        return suratKeluarDb.findAll().stream();
    }

    @Override
    public SuratKeluar getFileInput(String id) {
        Optional<SuratKeluar> optionalSuratKeluar = suratKeluarDb.findById(id);
        System.out.println(optionalSuratKeluar + "HAKSHDKJSAHJDHJASHDKAHSDJKSHKJDAHKDAHK" + id);
        if (optionalSuratKeluar.isPresent()) {
            return optionalSuratKeluar.get();
        } else {
            return null;
        }
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
    public List<SuratKeluar> searchSuratKeluar(Map<String, String> params, Date tanggalDibuat, String sort, String searchQuery) {
        List<SuratKeluar> suratKeluarList = suratKeluarDb.findAll();

        // Filter berdasarkan query pencarian
        if (searchQuery != null && !searchQuery.isEmpty()) {
            suratKeluarList = suratKeluarList.stream()
                    .filter(surat -> surat.getNomorArsip().toLowerCase().contains(searchQuery.toLowerCase())
                            || surat.getKategori().toLowerCase().contains(searchQuery.toLowerCase())
                            || surat.getPerihal().toLowerCase().contains(searchQuery.toLowerCase())
                            || surat.getPenerimaEksternal().toLowerCase().contains(searchQuery.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (tanggalDibuat != null) {
            suratKeluarList = suratKeluarDb.findByTanggalDibuat(tanggalDibuat);
        }
        
        return suratKeluarList;
    }

    @Override
    public SuratKeluar findSuratKeluarByID(String id) {
        Optional<SuratKeluar> optionalSuratKeluar = suratKeluarDb.findById(id);
        if (optionalSuratKeluar.isPresent()) {
            return optionalSuratKeluar.get();
        } else {
            return null;
        }
    }

    @Override
    public Map<String, Long> getJumlahSuratKeluarPerKategori() {
        Map<String, Long> mapSuratKeluarKategori = new HashMap<>();

        mapSuratKeluarKategori.put("Legal", suratKeluarDb.countByKategori("Legal"));
        mapSuratKeluarKategori.put("SDM", suratKeluarDb.countByKategori("SDM"));
        mapSuratKeluarKategori.put("Keuangan", suratKeluarDb.countByKategori("Keuangan"));
        mapSuratKeluarKategori.put("Sarana", suratKeluarDb.countByKategori("Sarana"));
        mapSuratKeluarKategori.put("Kemahasiswaan", suratKeluarDb.countByKategori("Kemahasiswaan"));
        mapSuratKeluarKategori.put("Lainnya", suratKeluarDb.countByKategori("Lainnya"));

        System.out.println(mapSuratKeluarKategori.toString());
        return mapSuratKeluarKategori;
    }

    @Override
    public Map<String, Integer> getJumlahSuratKeluarTahunIni() {
        LocalDate now = LocalDate.now();
        Date firstDayOfYear = Date.from(now.with(TemporalAdjusters.firstDayOfYear()).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date lastDayOfYear = Date.from(now.with(TemporalAdjusters.lastDayOfYear()).atTime(23, 59, 59, 999).atZone(ZoneId.systemDefault()).toInstant());

        List<SuratKeluar> allSuratKeluarThisYear = suratKeluarDb.findByTanggalDibuatBetween(firstDayOfYear, lastDayOfYear);
        Map<String, Integer> mapPerBulan = new LinkedHashMap<String, Integer>();

        String[] indonesianMonths = new String[] {"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        for (String bulan : indonesianMonths) {
            mapPerBulan.put(bulan, 0);
        }

        Calendar calendar = Calendar.getInstance();
        allSuratKeluarThisYear.forEach(surat -> {
            calendar.setTime(surat.getTanggalDibuat());
            mapPerBulan.merge(indonesianMonths[calendar.get(Calendar.MONTH)], 1, Integer::sum);
        });

        return mapPerBulan;
    }

    @Override
    public Map<String, Integer> getJumlahSuratKeluarBulanIni() {
        LocalDate now = LocalDate.now();
        Date firstDayOfMonth = Date.from(now.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date lastDayOfMonth = Date.from(now.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59, 999).atZone(ZoneId.systemDefault()).toInstant());

        List<SuratKeluar> allSuratKeluarThisMonth = suratKeluarDb.findByTanggalDibuatBetween(firstDayOfMonth, lastDayOfMonth);
        Map<String, Integer> mapPerMinggu = new LinkedHashMap<String, Integer>();
        
        int totalWeeks = now.with(TemporalAdjusters.lastDayOfMonth()).get(WeekFields.of(Locale.getDefault()).weekOfMonth());
        for (int i = 1; i <= totalWeeks; i++) {
            mapPerMinggu.put("Minggu ke-" + i, 0);
        }

        Calendar calendar = Calendar.getInstance();
        for (SuratKeluar surat : allSuratKeluarThisMonth) {
            calendar.setTime(surat.getTanggalDibuat());
            int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);
            mapPerMinggu.merge("Minggu ke-" + weekOfMonth, 1, Integer::sum);
        }

        return mapPerMinggu;
    }

    @Override
    public Map<String, Integer> getJumlahSuratKeluarMingguIni() {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfWeekLocal = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastDayOfWeekLocal = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        Date firstDayOfWeek = Date.from(firstDayOfWeekLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date lastDayOfWeek = Date.from(lastDayOfWeekLocal.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        List<SuratKeluar> allSuratKeluarThisWeek = suratKeluarDb.findByTanggalDibuatBetween(firstDayOfWeek, lastDayOfWeek);
        Map<String, Integer> mapPerHari = new LinkedHashMap<String, Integer>();
        
        String[] indonesianWeek = new String[] {"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"};
        for (String day : indonesianWeek) {
            mapPerHari.put(day, 0);
        }

        Calendar calendar = Calendar.getInstance();
        allSuratKeluarThisWeek.forEach(surat -> {
            calendar.setTime(surat.getTanggalDibuat());
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == 1) {
                mapPerHari.merge(indonesianWeek[6], 1, Integer::sum);
            } else {
                mapPerHari.merge(indonesianWeek[dayOfWeek-2], 1, Integer::sum);
            }
        });

        return mapPerHari;
    }
}

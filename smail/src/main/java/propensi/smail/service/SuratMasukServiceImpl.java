package propensi.smail.service;

import java.io.*;
import java.util.*;
import java.text.*;
import java.time.*;
import java.util.stream.*;
import java.time.temporal.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.transaction.Transactional;
import propensi.smail.model.*;
import propensi.smail.model.user.*;
import propensi.smail.repository.*;

@Service
@Transactional
public class SuratMasukServiceImpl implements SuratMasukService {

    @Autowired
    private SuratMasukDb suratMasukDb;

    @Autowired
    private PenggunaDb penggunaDb;

    @Autowired
    private SuratKeluarDb suratKeluarDb;

    @Autowired
    private PenggunaService penggunaService;

    @Override
    public SuratMasuk storeJudul(MultipartFile file, String judul, String kategori, String perihal, String pengirim) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            SuratMasuk suratMasuk = new SuratMasuk();
                suratMasuk.setNomorArsip(generateId(kategori));
                suratMasuk.setFile(file.getBytes());
                suratMasuk.setKategori(kategori);
                suratMasuk.setPerihal(perihal);
                suratMasuk.setTanggalDibuat(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
                // suratMasuk.setStatus(1);
                suratMasuk.setPengirim(pengirim);
                suratMasuk.setFileName(fileName);
                return suratMasukDb.save(suratMasuk);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + fileName, e);
        }
    }

    @Override
    public SuratMasuk store(MultipartFile file, String kategori, String perihal, String pengirim) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        //debug
        System.out.println("File Name: " + fileName);
        System.out.println(kategori);
        System.out.println(perihal);
        System.out.println(pengirim);

        try {
            SuratMasuk suratMasuk = new SuratMasuk();
                suratMasuk.setNomorArsip(generateId(kategori));
                suratMasuk.setFile(file.getBytes());
                suratMasuk.setKategori(kategori);
                suratMasuk.setPerihal(perihal);
                suratMasuk.setTanggalDibuat(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
                // suratMasuk.setStatus(1);
                suratMasuk.setPengirim(pengirim);
                suratMasuk.setFileName(fileName);
                return suratMasukDb.save(suratMasuk);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + fileName, e);
        }
    }
        
    @Override
    public SuratMasuk getFile(String id) {
        return suratMasukDb.findById(id).get();
    }

    @Override
    public Stream<SuratMasuk> getAllFiles() {
        return suratMasukDb.findAll().stream();
    }


    @Override
    public List<SuratMasuk> getAllSuratMasuk() {
        return suratMasukDb.findAll();
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
            long count = suratMasukDb.countByKategori(kategori);

            String idSuffix = String.format("%05d", count + 1);
            return "IN" + "-" + abbreviation + "-" + idSuffix;
        } else {
            throw new IllegalArgumentException("Invalid kategori: " + kategori);
        }
    }

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendEmail(String[] to, String subject, String body, SuratMasuk suratMasuk) throws MessagingException, IOException {
        // ubah status dan tembusan objek surat masuk       
        ArrayList<String> tembusan = suratMasuk.getTembusan();
        for (String email : to) {
            if (!tembusan.contains(email)){
                tembusan.add(email);
            }
        }
        suratMasuk.setTembusan(tembusan);
        suratMasuk.setIsDisposisi(true);
        suratMasukDb.save(suratMasuk);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String tanggalDibuat = dateFormat.format(suratMasuk.getTanggalDibuat());
        body = String.format("Yth Bapak/Ibu,\n"
        + "Surat yang terlampir di bawah ini ditujukan kepada Anda. "
        + "Berikut adalah keterangan mengenai surat tersebut.\n\n"
        + "Perihal  : %-20s %s\n" // %s untuk memasukkan nilai variabel, dan %-20s untuk mengatur panjang string
        + "Dari     : %-20s %s\n"
        + "Tanggal  : %-20s %s\n\n"
        + "Untuk informasi lebih lanjut, Anda dapat mengaksesnya melalui file yang terlampir atau hubungi (021)87962291\n\n\n"
        + "Salam,\n"
        + "Yayasan Tazkia\n"
        + "Jl. Ir. H. Djuanda No. 78, Bogor, Jawa Barat 16122\n",
        suratMasuk.getPerihal(), "", // Tambahkan spasi kosong untuk menjaga titik dua sejajar
        suratMasuk.getPengirim(), "",
        tanggalDibuat, ""); 

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, false); 
        helper.setFrom("instituttazkia.adm@gmail.com");
        helper.addAttachment(suratMasuk.getFileName(), new ByteArrayDataSource(suratMasuk.getFile(), "application/pdf")); // Specify the content type for the attachment
        
        mailSender.send(message);
    }

    @Override
    public List<SuratMasuk> searchSuratMasuk(Map<String, String> params, Date tanggalDibuat, String sort, String searchQuery) {
        List<SuratMasuk> suratMasukList = suratMasukDb.findAll();

        // Filter berdasarkan query pencarian
        if (searchQuery != null && !searchQuery.isEmpty()) {
            suratMasukList = suratMasukList.stream()
                    .filter(surat -> surat.getNomorArsip().toLowerCase().contains(searchQuery.toLowerCase())
                            || surat.getKategori().toLowerCase().contains(searchQuery.toLowerCase())
                            || surat.getPerihal().toLowerCase().contains(searchQuery.toLowerCase())
                            || surat.getPengirim().toLowerCase().contains(searchQuery.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (tanggalDibuat != null) {
            suratMasukList = suratMasukDb.findByTanggalDibuat(tanggalDibuat);
        }
        
        return suratMasukList;
    }

    @Override
    public List<SuratMasuk> getSuratBySearch(String search) {
        return suratMasukDb.findBySearch(search);
    }

    @Override
    public List<Pengguna> getAllPenandatangan() {
        List<Pengguna> listTembusan = penggunaDb.findAll().stream()
            .filter(user -> {
                String role = penggunaService.getRole(user);
                return role.equals("Pengurus");
            })
            .collect(Collectors.toList());
        return listTembusan;
    }

    @Override
    public List<SuratMasuk> getSuratMasukBySearchIsDisposisi(String search) {
        return suratMasukDb.findBySearchAndIsDisposisi(search);
    }

    @Override
    public List<SuratMasuk> getSuratMasukBySearchIsFollowUp(String search) {
        return suratMasukDb.findBySearchAndIsFollowUp(search);
    }

    @Override
    public List<SuratMasuk> getSuratMasukIsDisposisi() {
        return suratMasukDb.findByIsDisposisiTrue();
    }

    @Override
    public List<SuratMasuk> getSuratMasukIsFollowUp() {
        return suratMasukDb.findByIsFollowUpTrue();
    }
    
    @Override
    public Map<String, Long> getJumlahSuratMasukPerKategori() {
        Map<String, Long> mapSuratMasukKategori = new HashMap<>();

        mapSuratMasukKategori.put("Legal", suratMasukDb.countByKategori("Legal"));
        mapSuratMasukKategori.put("SDM", suratMasukDb.countByKategori("SDM"));
        mapSuratMasukKategori.put("Keuangan", suratMasukDb.countByKategori("Keuangan"));
        mapSuratMasukKategori.put("Sarana", suratMasukDb.countByKategori("Sarana"));
        mapSuratMasukKategori.put("Kemahasiswaan", suratMasukDb.countByKategori("Kemahasiswaan"));
        mapSuratMasukKategori.put("Lainnya", suratMasukDb.countByKategori("Lainnya"));

        System.out.println(mapSuratMasukKategori.toString());
        return mapSuratMasukKategori;
    }

    @Override
    public Map<String, Integer> getJumlahSuratMasukTahunIni() {
        LocalDate now = LocalDate.now();
        Date firstDayOfYear = Date.from(now.with(TemporalAdjusters.firstDayOfYear()).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date lastDayOfYear = Date.from(now.with(TemporalAdjusters.lastDayOfYear()).atTime(23, 59, 59, 999).atZone(ZoneId.systemDefault()).toInstant());

        List<SuratMasuk> allSuratMasukThisYear = suratMasukDb.findByTanggalDibuatBetween(firstDayOfYear, lastDayOfYear);
        Map<String, Integer> mapPerBulan = new LinkedHashMap<String, Integer>();

        String[] indonesianMonths = new String[] {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des"};
        for (String bulan : indonesianMonths) {
            mapPerBulan.put(bulan, 0);
        }

        Calendar calendar = Calendar.getInstance();
        allSuratMasukThisYear.forEach(surat -> {
            calendar.setTime(surat.getTanggalDibuat());
            mapPerBulan.merge(indonesianMonths[calendar.get(Calendar.MONTH)], 1, Integer::sum);
        });

        return mapPerBulan;
    }

    @Override
    public Map<String, Integer> getJumlahSuratMasukBulanIni() {
        LocalDate now = LocalDate.now();
        Date firstDayOfMonth = Date.from(now.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date lastDayOfMonth = Date.from(now.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59, 999).atZone(ZoneId.systemDefault()).toInstant());

        List<SuratMasuk> allSuratMasukThisMonth = suratMasukDb.findByTanggalDibuatBetween(firstDayOfMonth, lastDayOfMonth);
        Map<String, Integer> mapPerMinggu = new LinkedHashMap<String, Integer>();
        
        int totalWeeks = now.with(TemporalAdjusters.lastDayOfMonth()).get(WeekFields.of(Locale.getDefault()).weekOfMonth());
        for (int i = 1; i <= totalWeeks; i++) {
            mapPerMinggu.put("Minggu ke-" + i, 0);
        }

        Calendar calendar = Calendar.getInstance();
        for (SuratMasuk surat : allSuratMasukThisMonth) {
            calendar.setTime(surat.getTanggalDibuat());
            int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);
            mapPerMinggu.merge("Minggu ke-" + weekOfMonth, 1, Integer::sum);
        }

        return mapPerMinggu;
    }

    @Override
    public Map<String, Integer> getJumlahSuratMasukMingguIni() {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfWeekLocal = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastDayOfWeekLocal = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        Date firstDayOfWeek = Date.from(firstDayOfWeekLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date lastDayOfWeek = Date.from(lastDayOfWeekLocal.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        List<SuratMasuk> allSuratMasukThisWeek = suratMasukDb.findByTanggalDibuatBetween(firstDayOfWeek, lastDayOfWeek);
        Map<String, Integer> mapPerHari = new LinkedHashMap<String, Integer>();
        
        String[] indonesianWeek = new String[] {"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"};
        for (String day : indonesianWeek) {
            mapPerHari.put(day, 0);
        }

        Calendar calendar = Calendar.getInstance();
        allSuratMasukThisWeek.forEach(surat -> {
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

    @Override
    public Map<String, Integer> getJumlahSuratMasukPerStatus() {
        Map<String, Integer> mapSuratMasukStatus = new LinkedHashMap<String, Integer>();
        mapSuratMasukStatus.put("Disposisi", getSuratMasukIsDisposisi().size());
        mapSuratMasukStatus.put("Follow-up", getSuratMasukIsFollowUp().size());
        return mapSuratMasukStatus;
    }

}

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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
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
    public SuratKeluar storeTtd(RequestSurat requestSurat, MultipartFile file, String kategori, String jenisSurat, Pengguna penandatangan) throws IOException {
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
            suratKeluar.setPenandatangan(penandatangan);
            suratKeluar.setPengaju(requestSurat.getPengaju());

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
                suratKeluar.getRequestSurat().setStatus(5);
                suratKeluar.getRequestSurat().setTanggalSelesai(new Date());
                suratKeluarDb.save(suratKeluar);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to update SuratKeluar file: " + e.getMessage());
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
}

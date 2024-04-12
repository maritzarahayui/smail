package propensi.smail.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Base64;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import propensi.smail.model.SuratKeluar;
import propensi.smail.model.TemplateSurat;
import propensi.smail.repository.SuratKeluarDb;
import propensi.smail.repository.TemplateSuratDb;

@Service
public class SuratKeluarServiceImpl implements SuratKeluarService {

    @Autowired
    private SuratKeluarDb suratKeluarDb;

    @Autowired
    private TemplateSuratDb templateSuratDb;

    @Override
    public SuratKeluar store(MultipartFile file, String kategori, String perihal, String jenisSurat, String penerimaEksternal) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        //debug
        System.out.println("File Name: " + fileName);
        System.out.println(kategori);
        System.out.println(jenisSurat);
        System.out.println(penerimaEksternal);

        try {
            SuratKeluar suratKeluar = new SuratKeluar();
                suratKeluar.setNomorArsip(generateId(kategori));
                suratKeluar.setFile(file.getBytes());
                suratKeluar.setKategori(kategori);
                suratKeluar.setJenisSurat(jenisSurat);
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
    public SuratKeluar getFile(String id) {
        return suratKeluarDb.findById(id).get();
    }

    @Override
    public Stream<SuratKeluar> getAllFiles() {
        return suratKeluarDb.findAll().stream();
    }

    @Override
    public List<SuratKeluar> getAllSuratKeluar() {
        return suratKeluarDb.findAll();
    }

    @Override
    public String generateId(String kategori) {
        Map<String, String> kategoriMap = Map.of(
                "LEGAL", "LGL",
                "SDM", "SDM",
                "KEUANGAN", "KEU",
                "SARANA", "SAR",
                "LAINNYA", "LN"
        );

        String abbreviation = kategoriMap.get(kategori.toUpperCase());

        if (abbreviation != null) {
            long count = suratKeluarDb.countByKategori(kategori);

            String idSuffix = String.format("%05d", count + 1);
            return "OUT" + "-" + abbreviation + "-" + idSuffix;
        } else {
            throw new IllegalArgumentException("Invalid kategori: " + kategori);
        }
    }

    @Override
    public List<SuratKeluar> searchSuratKeluar(Map<String, String> params, Date tanggalDibuat, String sort, String searchQuery) {
        List<SuratKeluar> suratKeluarList = suratKeluarDb.findAll();

        // Filter berdasarkan query pencarian
        if (searchQuery != null && !searchQuery.isEmpty()) {
            suratKeluarList = suratKeluarList.stream()
                    .filter(surat -> surat.getNomorArsip().toLowerCase().contains(searchQuery.toLowerCase())
                            || surat.getKategori().toLowerCase().contains(searchQuery.toLowerCase())
                            || surat.getJenisSurat().toLowerCase().contains(searchQuery.toLowerCase())
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
}

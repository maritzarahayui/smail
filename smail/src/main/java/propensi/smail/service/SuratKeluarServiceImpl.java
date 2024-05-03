package propensi.smail.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.SuratKeluar;
import propensi.smail.model.SuratMasuk;
import propensi.smail.model.TemplateSurat;
import propensi.smail.model.user.Pengguna;
import propensi.smail.repository.RequestSuratDb;
import propensi.smail.repository.SuratKeluarDb;
import propensi.smail.repository.SuratMasukDb;
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

    // surat masuk db
    @Autowired
    private SuratMasukDb suratMasukDb;

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
            return "OUT" + "-" + abbreviation + "-" + idSuffix;
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
                    suratKeluar.setIsSigned(true);
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
    @Transactional
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
    public SuratKeluar storeArsipFollowUp(MultipartFile file, SuratMasuk arsipAwal, String perihal,
            String penerimaEksternal, Pengguna penandatangan) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            // bikin objek surat keluar
            SuratKeluar suratKeluar = new SuratKeluar();
                suratKeluar.setNomorArsip(generateId(arsipAwal.getKategori()));
                suratKeluar.setFile(file.getBytes());
                suratKeluar.setKategori(arsipAwal.getKategori());
                suratKeluar.setPerihal(perihal);
                suratKeluar.setTanggalDibuat(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
                suratKeluar.setIsSigned(false);
                suratKeluar.setPenerimaEksternal(penerimaEksternal);
                suratKeluar.setFileName(fileName);
                suratKeluar.setCurrentPenandatangan(penandatangan);
                suratKeluar.setArsipSurat(arsipAwal);                         

                // ubah status arsip awal
                arsipAwal.setStatus(3);
                suratMasukDb.save(arsipAwal);

                return suratKeluarDb.save(suratKeluar);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + fileName, e);
        }
    }

    @Override
    public List<SuratKeluar> getSuratKeluarByPenandatanganAndIsSigned(Pengguna penandatangan, Boolean isSigned) {
        return suratKeluarDb.findByCurrentPenandatanganAndIsSigned(penandatangan, isSigned);
    }

    @Override
    public List<SuratKeluar> getSuratKeluarByIsSigned(Boolean isSigned) {
        return suratKeluarDb.findByIsSigned(isSigned);
    }

    @Override
    @Transactional 
    public SuratKeluar getSuratKeluarByNomorArsip(String nomorArsip) {
        return suratKeluarDb.findByNomorArsip(nomorArsip);
    } 

    @Override
    @Transactional 
    public void updateFollowUpFile(String id, MultipartFile file) {
        // Retrieve the SuratKeluar object by suratKeluar nomorArsip
        SuratKeluar suratKeluar = suratKeluarDb.findByNomorArsip(id);

        try {
            System.out.println("masuk serv");

            // Check if a new file is uploaded
            if (file != null && !file.isEmpty()) {
                // Convert the file to byte array
                byte[] fileBytes = file.getBytes();

                // Set the file and file name
                suratKeluar.setFile(fileBytes);
                suratKeluar.setFileName(file.getOriginalFilename());
                suratKeluar.setIsSigned(true);
                suratKeluarDb.save(suratKeluar);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to update SuratKeluar file: " + e.getMessage());
        }
    }

    @Override
    public List<SuratKeluar> getSuratKeluarByCurrentPenandatangan(Pengguna penandatangan) {
        return suratKeluarDb.findByCurrentPenandatangan(penandatangan);
    }
    
}

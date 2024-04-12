package propensi.smail.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.SuratKeluar;
import propensi.smail.model.user.Pengguna;
import propensi.smail.repository.RequestSuratDb;
import propensi.smail.repository.SuratKeluarDb;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SuratKeluarServiceImpl implements SuratKeluarService {
    @Autowired
    SuratKeluarDb suratKeluarDb;

    @Autowired
    RequestSuratDb requestSuratDb;
    
    @Override
    @Transactional
    public SuratKeluar store(RequestSurat requestSurat, MultipartFile file, String kategori, String jenisSurat, Pengguna penandatangan) throws IOException {
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
    public SuratKeluar getFile(String id) {
        Optional<SuratKeluar> optionalSuratKeluar = Optional.ofNullable(findSuratKeluarByRequestID(id));
        if (optionalSuratKeluar.isPresent()) {
            return optionalSuratKeluar.get();
        } else {
            return null;
        }
    }

    public SuratKeluar findSuratKeluarByRequestID(String id) {
        List<SuratKeluar> suratKeluars = suratKeluarDb.findAll();

        for (SuratKeluar suratKeluar : suratKeluars) {
            if (suratKeluar.getRequestSurat().getId().equals(id)) {
                return suratKeluar;
            }
        }
        return null;
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
                suratKeluarDb.save(suratKeluar);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to update SuratKeluar file: " + e.getMessage());
        }
    }

}

package propensi.smail.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import propensi.smail.model.SuratMasuk;
import propensi.smail.repository.SuratMasukDb;

@Service
public class SuratMasukServiceImpl implements SuratMasukService {

    @Autowired
    private SuratMasukDb suratMasukDb;

    @Override
    public SuratMasuk store(MultipartFile file, String kategori, String perihal, Date tanggalDibuat, int status, String pengirim, String tembusan) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            SuratMasuk suratMasuk = new SuratMasuk();
                suratMasuk.setNomorArsip(generateId(kategori));
                suratMasuk.setFile(file.getBytes());
                suratMasuk.setKategori(kategori);
                suratMasuk.setPerihal(perihal);
                suratMasuk.setTanggalDibuat(tanggalDibuat);
                suratMasuk.setStatus(status);
                suratMasuk.setPengirim(pengirim);
                suratMasuk.setTembusan(tembusan);
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
    
}

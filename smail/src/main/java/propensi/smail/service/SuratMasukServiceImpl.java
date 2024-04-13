package propensi.smail.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
import propensi.smail.model.SuratMasuk;
import propensi.smail.repository.SuratMasukDb;

@Service
public class SuratMasukServiceImpl implements SuratMasukService {

    @Autowired
    private SuratMasukDb suratMasukDb;

    @Override
    public SuratMasuk store(MultipartFile file, String judul, String kategori, String perihal, String pengirim) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            SuratMasuk suratMasuk = new SuratMasuk();
                suratMasuk.setNomorArsip(generateId(kategori));
                suratMasuk.setFile(file.getBytes());
                suratMasuk.setJudul(judul);
                suratMasuk.setKategori(kategori);
                suratMasuk.setPerihal(perihal);
                suratMasuk.setTanggalDibuat(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
                suratMasuk.setStatus(1);
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
        suratMasuk.setStatus(2);
        suratMasuk.setTembusan(to);
        suratMasukDb.save(suratMasuk);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        body = String.format("Yth Bapak/Ibu,\n"
                    + "Surat yang terlampir di bawah ini ditujukan kepada Anda. "
                    + "Berikut adalah keterangan mengenai surat tersebut.\n\n"
                    + "Perihal  : %-20s %s\n" // %s untuk memasukkan nilai variabel, dan %-20s untuk mengatur panjang string
                    + "Dari     : %-20s %s\n"
                    + "Tanggal  : %-20s %s\n\n"
                    + "Untuk informasi lebih lanjut, Anda dapat mengaksesnya melalui file yang terlampir atau hubungi (021)87962291\n\n\n\n"
                    + "Salam,\n"
                    + "Yayasan Tazkia\n"
                    + "Jl. Ir. H. Djuanda No. 78, Bogor, Jawa Barat 16122\n",
                    suratMasuk.getPerihal(), "", // Tambahkan spasi kosong untuk menjaga titik dua sejajar
                    suratMasuk.getPengirim(), "",
                    suratMasuk.getTanggalDibuat(), "");
        
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, false); 
        helper.setFrom("instituttazkia.adm@gmail.com");
        helper.addAttachment(suratMasuk.getFileName(), new ByteArrayDataSource(suratMasuk.getFile(), "application/pdf")); // Specify the content type for the attachment
        
        mailSender.send(message);

        

        // debug
        System.out.println("Email sent to: "  );
        // iterate
        for (String email : to) {
            System.out.println(email);
        }
        // status
        System.out.println("Status: " + suratMasuk.getStatus());
    }


    

}

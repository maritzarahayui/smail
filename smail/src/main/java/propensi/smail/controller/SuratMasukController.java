package propensi.smail.controller;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Base64;
import java.util.Date;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.util.StringBuilderFormattable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import propensi.smail.model.SuratMasuk;
import propensi.smail.service.SuratMasukService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/api/surat-masuk")
public class SuratMasukController {

    @Autowired
    private SuratMasukService suratMasukService;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("kategori") String kategori, @RequestParam("perihal") String perihal, @RequestParam("tanggalDibuat") String tanggalDibuat, @RequestParam("status") int status, @RequestParam("pengirim") String pengirim, @RequestParam("tembusan") String tembusan) throws ParseException {
        String message = "";
        //convert type of tanggalDibuat from String to Date
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date tanggalDibuatDate = formatter.parse(tanggalDibuat);

        try {
            SuratMasuk suratMasuk = suratMasukService.store(file, kategori, perihal, tanggalDibuatDate, status, pengirim, tembusan);
            // dapatkan surat masuknya

            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            // redirect to detail surat masuk
            return "redirect:/api/surat-masuk/detail/" + suratMasuk.getNomorArsip();
        }catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            return "redirect:/api/surat-masuk/form";
        }
    }

    @GetMapping("/files")
    public ResponseEntity<List<String>> getListFiles() {
        List<String> fileNames = suratMasukService.getAllFiles().map(fileName -> {
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/surat-masuk/files/").path(fileName.getNomorArsip()).toUriString();
            return fileDownloadUri;
        }).collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(fileNames);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String id) {
        SuratMasuk file = suratMasukService.getFile(id);

        if (file != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", file.getFileName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(file.getFile());
        } else {
            // Handle the case where file is not found
            return ResponseEntity.notFound().build();
        }
    }

    //get all surat masuk
    @GetMapping("/all")
    public String getAllSuratMasuk(Model model) {
        List<SuratMasuk> suratMasukList = suratMasukService.getAllSuratMasuk();
        //masukan ke thymeleaf
        model.addAttribute("suratMasukList", suratMasukList);
        return "daftar-arsip-tes";
    }

    // Metode untuk menampilkan preview PDF
    @GetMapping("/detail/{id}")
    public String previewPDF(@PathVariable("id") String id, Model model) throws IOException {
        SuratMasuk suratMasuk = suratMasukService.getFile(id);
        byte[] pdf = suratMasuk.getFile();

        // Mengonversi konten PDF ke Base64
        String base64PDF = Base64.getEncoder().encodeToString(pdf);

        model.addAttribute("base64PDF", base64PDF);
        model.addAttribute("suratMasuk", suratMasuk);
        model.addAttribute("statusText", getStatusText(suratMasuk.getStatus()));
        return "detail-arsip-tes"; 
    }

    // Fungsi untuk mengonversi status menjadi teks
    public String getStatusText(int status) {
        switch (status) {
            case 1:
                return "Diarsipkan";
            case 2:
                return "Disposisi";
            case 3:
                return "Follow Up";
            default:
                return "Status Tidak Diketahui";
        }
    }
    
    // route to form-upload-surat
    @GetMapping("/form")
    public String formUploadSurat(Model model) {
        return "form-arsip-tes";
    }

    @GetMapping("/form-arsip")
    public String form(Model model) {
        return "form-surat-masuk";
    }

    // route to semua-surat-masuk
    @GetMapping("/daftar-arsip")
    public String semuaSuratMasuk(Model model) {
        return "daftar-arsip-tes";
    }

    // route to detail-surat-masuk
    @GetMapping("/detail-surat-masuk")
    public String detailSuratMasuk(Model model) {
        return "detail-surat-masuk";
    }
    
}

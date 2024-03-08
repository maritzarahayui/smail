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
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("kategori") String kategori, @RequestParam("perihal") String perihal, @RequestParam("tanggalDibuat") String tanggalDibuat, @RequestParam("status") int status, @RequestParam("pengirim") String pengirim, @RequestParam("tembusan") String tembusan) throws ParseException {
        String message = "";
        //convert type of tanggalDibuat from String to Date
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date tanggalDibuatDate = formatter.parse(tanggalDibuat);

        try {
            suratMasukService.store(file, kategori, perihal, tanggalDibuatDate, status, pengirim, tembusan);
            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
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

    // download file
    @GetMapping("/files/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable String id) {
        SuratMasuk file = suratMasukService.getFile(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .body(file.getFile());
    }

    // get detail of surat masuk
    // @GetMapping("/detail/{id}")
    // public ResponseEntity<SuratMasuk> getDetailSuratMasuk(@PathVariable String id) {
    //     SuratMasuk suratMasuk = suratMasukService.getFile(id);
    //     return ResponseEntity.ok().body(suratMasuk);
    // }


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
        SuratMasuk file = suratMasukService.getFile(id);
        byte[] pdf = file.getFile();

        // Mengonversi konten PDF ke Base64
        String base64PDF = Base64.getEncoder().encodeToString(pdf);

        model.addAttribute("base64PDF", base64PDF);
        model.addAttribute("template", file); // Menambahkan objek template ke model
        return "detail-arsip-tes"; // Mengembalikan tampilan pratinjau PDF menggunakan Thymeleaf
    }

    
    // route to form-upload-surat
    @GetMapping("/form")
    public String formUploadSurat(Model model) {
        return "form-arsip-tes";
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

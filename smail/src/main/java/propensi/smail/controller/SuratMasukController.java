package propensi.smail.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import propensi.smail.model.SuratMasuk;
import propensi.smail.model.user.Pengguna;
import propensi.smail.repository.PenggunaDb;
import propensi.smail.service.PenggunaService;
import propensi.smail.service.SuratMasukService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Controller
@RequestMapping("/surat-masuk")
public class SuratMasukController {

    @Autowired
    private SuratMasukService suratMasukService;

    @Autowired
    PenggunaDb penggunaDb;

    @Autowired
    PenggunaService penggunaService;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("kategori") String kategori, 
        @RequestParam("perihal") String perihal, @RequestParam("pengirim") String pengirim, 
        @RequestParam("tembusan") String tembusan, Authentication auth, Model model) throws ParseException {
        try {
            SuratMasuk suratMasuk = suratMasukService.store(file, kategori, perihal, pengirim, tembusan);
            return "redirect:/surat-masuk/detail/" + suratMasuk.getNomorArsip();
        }catch (Exception e) {
            //debug
            System.out.println("error kenapee:" + e.getMessage());
            return "redirect:/surat-masuk/form";
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String id, Authentication auth) {
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
    public String getAllSuratMasuk(Model model, Authentication auth, 
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "tanggalDibuat", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date tanggalDibuat) {
        List<SuratMasuk> suratMasukList;
        if (status != null && !status.isEmpty()) {
            switch (status) {
                case "Diarsipkan":
                    suratMasukList = suratMasukService.getSuratMasukByStatus(1);
                    break;
                case "Follow Up":
                    suratMasukList = suratMasukService.getSuratMasukByStatus(2);
                    break;
                case "Disposisi":
                    suratMasukList = suratMasukService.getSuratMasukByStatus(3);
                    break;
                default:
                    suratMasukList = suratMasukService.getAllSuratMasuk();
                    break;
            }
        } else {
            suratMasukList = suratMasukService.getAllSuratMasuk();
        }

        // Filter surat masuk berdasarkan tanggal dibuat jika parameter tanggalDibuat diberikan
        if (tanggalDibuat != null) {
            suratMasukList = suratMasukList.stream()
                    .filter(surat -> surat.getTanggalDibuat().equals(tanggalDibuat))
                    .collect(Collectors.toList());
        }

        model.addAttribute("suratMasukList", suratMasukList);

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        return "daftar-surat-masuk";
    }

    // Metode untuk menampilkan preview PDF
    @GetMapping("/detail/{id}")
    public String previewPDF(@PathVariable("id") String id, Model model, Authentication auth) throws IOException {
        SuratMasuk suratMasuk = suratMasukService.getFile(id);
        byte[] pdf = suratMasuk.getFile();

        // Mengonversi konten PDF ke Base64
        String base64PDF = Base64.getEncoder().encodeToString(pdf);

        model.addAttribute("base64PDF", base64PDF);
        model.addAttribute("suratMasuk", suratMasuk);
        model.addAttribute("statusText", getStatusText(suratMasuk.getStatus()));

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        // return "detail-surat-tes"; 
        return "detail-surat-masuk"; 

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
    public String formUploadSurat(Model model, Authentication auth) {

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        return "form-surat-masuk";
    }

    @GetMapping("/daftar")
    public String form(Model model) {
        return "daftar-surat-masuk";
    }

    // route to semua-surat-masuk
    @GetMapping("/daftar-arsip")
    public String semuaSuratMasuk(Model model, Authentication auth) {
        return "daftar-arsip-tes";
    }

    // route to detail-surat-masuk
    @GetMapping("/detail-surat-masuk")
    public String detailSuratMasuk(Model model, Authentication auth) {
        return "detail-surat-masuk";
    }

    @GetMapping("/search")
    public String searchSuratMasuk(@RequestParam Map<String, String> params, Model model, Authentication auth,
                                @RequestParam(value = "tanggalDibuat", required = false)
                                @DateTimeFormat(pattern = "yyyy-MM-dd") Date tanggalDibuat,
                                @RequestParam(value = "sort", defaultValue = "tanggalDibuatAsc") String sort) {

        // Mendapatkan nilai pencarian dari parameter "q"
        String searchQuery = params.get("q");

        // Melakukan pencarian dan filtering surat masuk berdasarkan nilai pencarian
        List<SuratMasuk> suratMasukList = suratMasukService.searchSuratMasuk(params, tanggalDibuat, sort, searchQuery);
        model.addAttribute("suratMasukList", suratMasukList);

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        return "daftar-surat-masuk";
    }


}

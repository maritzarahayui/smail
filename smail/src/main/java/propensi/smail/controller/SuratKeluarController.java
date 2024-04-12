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

import propensi.smail.model.SuratKeluar;
import propensi.smail.model.user.Pengguna;
import propensi.smail.repository.PenggunaDb;
import propensi.smail.service.PenggunaService;
import propensi.smail.service.SuratKeluarService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Controller
@RequestMapping("/surat-keluar")
public class SuratKeluarController {

    @Autowired
    private SuratKeluarService suratKeluarService;

    @Autowired
    PenggunaDb penggunaDb;

    @Autowired
    PenggunaService penggunaService;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("kategori") String kategori,  
        @RequestParam("perihal") String perihal, @RequestParam("jenis_surat") String jenisSurat, 
        @RequestParam("penerima_eksternal") String penerima_eksternal, 
        Authentication auth, Model model) throws ParseException {
        try {
            SuratKeluar suratKeluar= suratKeluarService.store(file, kategori, perihal, jenisSurat, penerima_eksternal);
            return "redirect:/surat-keluar/detail/" + suratKeluar.getNomorArsip();
        }catch (Exception e) {
            //debug
            System.out.println("error kenapee:" + e.getMessage());
            return "redirect:/surat-keluar/form";
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String id, Authentication auth) {
        SuratKeluar file = suratKeluarService.getFile(id);

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

    //get all surat keluar (diarsipkan)
    @GetMapping("/all")
    public String getAllSuratKeluar(Model model, Authentication auth,
        @RequestParam(value = "tanggalDibuat", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date tanggalDibuat) {
        List<SuratKeluar> suratKeluarList = suratKeluarService.getAllSuratKeluar();
        
        // Filter surat masuk berdasarkan tanggal dibuat jika parameter tanggalDibuat diberikan
        if (tanggalDibuat != null) {
            suratKeluarList = suratKeluarList.stream()
                    .filter(surat -> surat.getTanggalDibuat().equals(tanggalDibuat))
                    .collect(Collectors.toList());
        }

        model.addAttribute("suratKeluarList", suratKeluarList);

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

        return "daftar-surat-keluar";
    }

    // Metode untuk menampilkan preview PDF
    @GetMapping("/detail/{id}")
    public String previewPDF(@PathVariable("id") String id, Model model, Authentication auth) throws IOException {
        SuratKeluar suratKeluar = suratKeluarService.getFile(id);
        byte[] pdf = suratKeluar.getFile();

        // Mengonversi konten PDF ke Base64
        String base64PDF = Base64.getEncoder().encodeToString(pdf);

        model.addAttribute("base64PDF", base64PDF);
        model.addAttribute("suratKeluar", suratKeluar);

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
        return "detail-surat-keluar"; 

    }
    
    // route to form-upload-surat
    @GetMapping("/form")
    public String formUploadSurat(Model model, Authentication auth) {

        String role = "";
        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                role = penggunaService.getRole(pengguna);
                model.addAttribute("role", role);
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        Map<String, List<String>> jenisSuratByKategori = suratKeluarService.generateJenisSuratByKategoriAndRole(role);
        model.addAttribute("jenisSuratByKategori", jenisSuratByKategori);

        return "form-surat-keluar";
    }

    @GetMapping("/daftar")
    public String form(Model model) {
        return "daftar-surat-keluar";
    }

    // route to semua-surat-keluar
    @GetMapping("/daftar-arsip")
    public String semuaSuratKeluar(Model model, Authentication auth) {
        return "daftar-arsip-tes";
    }

    // route to detail-surat-keluar
    @GetMapping("/detail-surat-keluar")
    public String detailSuratKeluar(Model model, Authentication auth) {
        return "detail-surat-keluar";
    }

    @GetMapping("/search")
    public String searchSuratKeluar(@RequestParam Map<String, String> params, Model model, Authentication auth,
                                @RequestParam(value = "tanggalDibuat", required = false)
                                @DateTimeFormat(pattern = "yyyy-MM-dd") Date tanggalDibuat,
                                @RequestParam(value = "sort", defaultValue = "tanggalDibuatAsc") String sort) {

        // Mendapatkan nilai pencarian dari parameter "q"
        String searchQuery = params.get("q");

        // Melakukan pencarian dan filtering surat keluar berdasarkan nilai pencarian
        List<SuratKeluar> suratKeluarList = suratKeluarService.searchSuratKeluar(params, tanggalDibuat, sort, searchQuery);
        model.addAttribute("suratKeluarList", suratKeluarList);

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
        return "daftar-surat-keluar";
    }

    

}

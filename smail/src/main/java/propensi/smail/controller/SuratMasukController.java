package propensi.smail.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Base64;
import java.io.IOException;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import propensi.smail.model.SuratMasuk;
import propensi.smail.model.user.Pengguna;
import propensi.smail.model.Email;
import propensi.smail.repository.PenggunaDb;
import propensi.smail.service.PenggunaService;
import propensi.smail.service.SuratMasukService;

import org.springframework.web.bind.annotation.RequestMapping;
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
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("judul") String judul,  
        @RequestParam("kategori") String kategori, @RequestParam("perihal") String perihal, @RequestParam("pengirim") String pengirim, 
        Authentication auth, Model model) throws ParseException {
        try {
            SuratMasuk suratMasuk = suratMasukService.store(file, judul, kategori, perihal, pengirim);
            return "redirect:/surat-masuk/detail/" + suratMasuk.getNomorArsip();
        }catch (Exception e) {
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
    public String getAllSuratMasuk(Model model, Authentication auth) {
        List<SuratMasuk> suratMasukList = suratMasukService.getAllSuratMasuk();
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
        // debug
        System.out.println("debug coy");
        // debug iterate tembusan
        System.out.println("tembusan");
        if (suratMasuk.getTembusan() != null) {
            for (String tembusan : suratMasuk.getTembusan()) {
                System.out.println(tembusan);
            }
        }
        System.out.println(suratMasuk.getStatus());

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

    // root to disposisi arsip surat dengan id tertentu
    @GetMapping("/disposisi/{id}")
    public String disposisiSurat(@PathVariable("id") String id, Model model, Authentication auth) {
        SuratMasuk suratMasuk = suratMasukService.getFile(id); // You need to implement this method
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
        return "disposisi";
    }

    // // route to send email 
    @GetMapping("/send/{id}")
    public String sendEmail(@PathVariable("id") String id, @RequestParam("to") String to, Model model, Authentication auth) throws MessagingException, IOException {
        SuratMasuk file = suratMasukService.getFile(id);
        String[] recipients = to.split(","); 
        
        suratMasukService.sendEmail(recipients, file.getPerihal(), "", file);
        return "redirect:/surat-masuk/detail/" + id;
    }

    // route to follow up arsip surat 
    @GetMapping("/follow-up/{id}")
    public String followUpSurat(@PathVariable("id") String id, Model model, Authentication auth) {
        SuratMasuk suratMasuk = suratMasukService.getFile(id); // You need to implement this method
        model.addAttribute("suratMasuk", suratMasuk);

        List<Pengguna> listTembusan = penggunaDb.findAll().stream()
                .filter(user -> {
                    String role = penggunaService.getRole(user);
                    return role.equals("Dosen") || role.equals("Pengurus");
                })
                .collect(Collectors.toList());
        model.addAttribute("listTembusan", listTembusan);
        
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
        return "follow-up";
    }
    
}

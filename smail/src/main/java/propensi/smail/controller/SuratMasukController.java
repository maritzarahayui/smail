package propensi.smail.controller;

import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.text.ParseException;
import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import propensi.smail.model.*;
import propensi.smail.model.user.*;
import propensi.smail.repository.*;
import propensi.smail.service.*;

@Controller
@RequestMapping("/surat-masuk")
public class SuratMasukController {

    @Autowired
    private SuratMasukService suratMasukService;

    @Autowired
    PenggunaDb penggunaDb;

    @Autowired
    PenggunaService penggunaService;

    @Autowired
    private SuratMasukDb suratMasukDb;

    @Autowired
    private SuratKeluarService suratKeluarService;

    @Autowired
    private SuratKeluarDb suratKeluarDb;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("kategori") String kategori, 
        @RequestParam("perihal") String perihal, @RequestParam("pengirim") String pengirim, 
        Authentication auth, Model model) throws ParseException {
        try {
            SuratMasuk suratMasuk = suratMasukService.store(file, kategori, perihal, pengirim);
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
            return ResponseEntity.ok().headers(headers).body(file.getFile());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public String getAllSuratMasuk(Model model, Authentication auth, @RequestParam(required = false) String activeTab) {

        List<SuratMasuk> allSuratMasuk = suratMasukService.getAllSuratMasuk();
        List<SuratMasuk> suratMasukFollowUp = suratMasukService.getSuratMasukIsDisposisi();
        List<SuratMasuk> suratMasukDisposisi = suratMasukService.getSuratMasukIsFollowUp();

        model.addAttribute("allSuratMasuk", allSuratMasuk);
        model.addAttribute("suratMasukFollowUp", suratMasukFollowUp);
        model.addAttribute("suratMasukDisposisi", suratMasukDisposisi);
        model.addAttribute("activeTab", activeTab != null ? activeTab : "#semua");
        
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

    @GetMapping("/detail/{id}")
    public String previewPDF(@PathVariable("id") String id, Model model, Authentication auth) throws IOException {
        SuratMasuk suratMasuk = suratMasukService.getFile(id);
        byte[] pdf = suratMasuk.getFile();
        String base64PDF = Base64.getEncoder().encodeToString(pdf);

        model.addAttribute("base64PDF", base64PDF);
        model.addAttribute("suratMasuk", suratMasuk);

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

        return "detail-surat-masuk"; 

    }

    public String getStatusText(int status) {
        switch (status) {
            case 1:
                return "Diarsipkan";
            case 2:
                return "Disposisi";
            case 3:
                return "Mengajukan Follow Up";
            default:
                return "Status Tidak Diketahui";
        }
    }
    
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

    @GetMapping("/disposisi/{id}")
    public String disposisiSurat(@PathVariable("id") String id, Model model, Authentication auth) {
        SuratMasuk suratMasuk = suratMasukService.getFile(id);
        model.addAttribute("suratMasuk", suratMasuk);

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

    @GetMapping("/send/{id}")
    public String sendEmail(@PathVariable("id") String id, @RequestParam("to") String to, Model model, Authentication auth) throws MessagingException, IOException {
        SuratMasuk file = suratMasukService.getFile(id);
        String[] recipients = to.split(","); 
        
        suratMasukService.sendEmail(recipients, file.getPerihal(), "", file);
        return "redirect:/surat-masuk/detail/" + id;
    }

    @GetMapping("/follow-up/{id}")
    public String followUpSurat(@PathVariable("id") String id, Model model, Authentication auth) {
        SuratMasuk suratMasuk = suratMasukService.getFile(id); 
        model.addAttribute("suratMasuk", suratMasuk);
        List<Pengguna> penandatangan = suratMasukService.getAllPenandatangan();
        model.addAttribute("penandatangan", penandatangan);
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
    
    @PostMapping("/follow-up/{id}")
    public String followUpSurat(@PathVariable("id") String id, @RequestParam("file") MultipartFile file, @RequestParam("perihal") String perihal, @RequestParam("penandatangan") String idPenandatangan, Model model, Authentication auth) throws ParseException {
        SuratMasuk arsipAwal = suratMasukService.getFile(id);
        Pengguna penandatangan = penggunaDb.findById(idPenandatangan).get();
        String penerimaEksternal = arsipAwal.getPengirim();
        SuratKeluar arsipFollowUp = suratKeluarService.storeArsipFollowUp(file, arsipAwal, perihal, penerimaEksternal, penandatangan);

        arsipAwal.setPenandatangan(penandatangan);
        suratMasukDb.save(arsipAwal);

        model.addAttribute("suratMasuk", arsipAwal);
        
        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
                arsipFollowUp.setPengaju(pengguna);
                suratKeluarDb.save(arsipFollowUp);
            } else {
                return "auth-failed";
            }
        }

        model.addAttribute("noArsipAwal", arsipAwal.getNomorArsip());
        return "redirect:/surat-masuk/all";
    }
}

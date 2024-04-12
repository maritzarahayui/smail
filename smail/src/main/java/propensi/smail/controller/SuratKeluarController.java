package propensi.smail.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import propensi.smail.dto.RequestAndFieldDataDTO;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.RequestTemplate;
import propensi.smail.model.SuratKeluar;
import propensi.smail.model.TemplateSurat;
import propensi.smail.model.user.Pengguna;
import propensi.smail.repository.PenggunaDb;
import propensi.smail.repository.RequestSuratDb;
import propensi.smail.repository.SuratKeluarDb;
import propensi.smail.service.PenggunaService;
import propensi.smail.service.RequestService;
import propensi.smail.service.SuratKeluarService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SuratKeluarController {
    @Autowired
    private RequestService requestService;

    @Autowired
    private SuratKeluarService suratKeluarService;

    @Autowired
    private SuratKeluarDb suratKeluarDb;

    @Autowired
    private PenggunaDb penggunaDb;

    @Autowired
    private RequestSuratDb requestSuratDb;

    @Autowired
    private PenggunaService penggunaService;

    @GetMapping("/admin/detail/{id}/selesai")
    public String detailRequestSuratAdmin(@PathVariable("id") String id, Model model, Authentication auth)  throws IOException {
        RequestSurat requestSurats = requestService.getRequestSuratById(id);
        model.addAttribute("requestSurats", requestSurats);

        SuratKeluar suratKeluar = suratKeluarService.getFile(id);
        byte[] pdf = suratKeluar.getFile();

        SuratKeluar suratKeluar1 = requestSurats.getSurat();
        model.addAttribute("outgoing", suratKeluar1);

        System.out.println("ID: " + id);
        System.out.println("Request Surats: " + requestSurats);
        System.out.println("Surat Keluar: " + suratKeluar);

        // System.out.println(Arrays.toString(pdf));

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

        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(1, "Diajukan");
        statusMap.put(3, "Ditolak");
        statusMap.put(4, "Diproses");
        statusMap.put(5, "Selesai");

        model.addAttribute("statusMap", statusMap);

        return "admin-detail-request";
    }

    @GetMapping("/admin/detail/{id}/process")
    public String detailOnProcessRequestSuratAdmin(@PathVariable("id") String id, Model model, Authentication auth) {
        RequestSurat requestSurats = requestService.getRequestSuratById(id);
        model.addAttribute("requestSurats", requestSurats);

        SuratKeluar file = suratKeluarService.getFile(id);

        if (file != null) {
            byte[] pdf = file.getFile();

            // Convert PDF content to Base64
            String base64PDF = Base64.getEncoder().encodeToString(pdf);

            model.addAttribute("base64PDF", base64PDF);
            model.addAttribute("template", file); // Add the template object to the model
        }

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

        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(1, "Diajukan");
        statusMap.put(3, "Ditolak");
        statusMap.put(4, "Diproses");
        statusMap.put(5, "Selesai");

        model.addAttribute("statusMap", statusMap);

        List<Pengguna> listTembusan = penggunaDb.findAll().stream()
                .filter(user -> {
                    String role = penggunaService.getRole(user);
                    return role.equals("Dosen") || role.equals("Pengurus");
                })
                .collect(Collectors.toList());

        if (requestSurats.getSurat() != null) {
            SuratKeluar suratKeluar = requestSurats.getSurat();
            model.addAttribute("suratKeluar", suratKeluar);
        }

        model.addAttribute("listTembusan", listTembusan);

        return "admin-detail-diproses";
    }

    @PostMapping("/admin/detail/{id}/process")
    public String createSuratKeluar(@PathVariable("id") String requestSuratId,
                                    @RequestParam("file") MultipartFile file,
                                    @RequestParam("kategori") String kategori,
                                    @RequestParam("jenisSurat") String jenisSurat,
                                    @RequestParam("penandatangan") String penandatanganId,
                                    Model model) {
        try {
            System.out.println("masuk?");
            RequestSurat requestSurat = requestService.getRequestSuratById(requestSuratId);
            Pengguna pengguna = penggunaService.getPenggunaById(penandatanganId);

//            suratKeluarService.store(requestSurat, file, kategori, jenisSurat, pengguna);

            // Create and store SuratKeluar first
            SuratKeluar suratKeluar = suratKeluarService.store(requestSurat, file, kategori, jenisSurat, pengguna);

            // Associate SuratKeluar with RequestSurat and save RequestSurat
            requestSurat.setSurat(suratKeluar);
            requestSuratDb.save(requestSurat);

            System.out.println("UDaH MASUK ANJAAAYYY");

            return "redirect:/admin/request/process";
        } catch (Exception e) {
            System.out.println("error kenapee:" + e.getMessage());
            return "redirect:/admin/request/process";
        }
    }

    @GetMapping("/pengurus/request")
    public String showAllRequestsPengurus(Model model, Authentication auth) {
        List<RequestSurat> requestSurats = requestService.getAllOnProcessRequestsSurat();
        model.addAttribute("requestSurats", requestSurats);

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

        return "pengurus-ttd-request";
    }

    @GetMapping("/pengurus/detail/{id}")
    public String detailRequestSuratPengurus(@PathVariable("id") String id, Model model, Authentication auth)  throws IOException {
        RequestSurat requestSurats = requestService.getRequestSuratById(id);
        model.addAttribute("requestSurats", requestSurats);

        SuratKeluar suratKeluar = suratKeluarService.getFile(id);
        byte[] pdf = suratKeluar.getFile();

        SuratKeluar suratKeluar1 = requestSurats.getSurat();
        model.addAttribute("outgoing", suratKeluar1);

        System.out.println("ID: " + id);
        System.out.println("Request Surats: " + requestSurats);
        System.out.println("Surat Keluar: " + suratKeluar);

        // System.out.println(Arrays.toString(pdf));

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

        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(1, "Diajukan");
        statusMap.put(3, "Ditolak");
        statusMap.put(4, "Diproses");
        statusMap.put(5, "Selesai");

        model.addAttribute("statusMap", statusMap);

        return "pengurus-detail-request"; 
    }

    @PostMapping("/ttd/update/{id}")
    public String updateTTD(@PathVariable("id") String id,
                                 @RequestParam("file") MultipartFile file,
                                 Model model, Authentication auth) {
        String message = "";

        try {
            System.out.println("masukkkkkkkkkk");
            // Update the SuratKeluar file
            suratKeluarService.updateSuratKeluarFile(id, file);

            message = "PDF updated successfully";
            System.out.println(message);
            model.addAttribute("message", message);
            return "redirect:/pengurus/detail/{id}";
        } catch (Exception e) {
            message = "Failed to update the template: " + e.getMessage();
            System.out.println(message);
            model.addAttribute("errorMessage", message);
            return "redirect:/pengurus/detail/{id}";
        }
    }


    @GetMapping("/ttd/request")
    @Transactional(readOnly = true)
    public String showAllRequestsTTD(Model model, Authentication auth) {
//        List<RequestSurat> requestSurats = requestService.getAllOnProcessRequestsSurat();
//        model.addAttribute("requestSurats", requestSurats);

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                String penandatanganId = pengguna.getId();
                List<RequestSurat> requestSurats = requestService.getAllRequestSuratByPenandatanganId(penandatanganId);
                model.addAttribute("requestSurats", requestSurats);
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        return "pengurus-ttd-request";
    }
}
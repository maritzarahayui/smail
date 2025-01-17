package propensi.smail.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import propensi.smail.dto.RequestAndFieldDataDTO;
import propensi.smail.model.*;
import propensi.smail.model.user.*;
import propensi.smail.repository.*;
import propensi.smail.service.*;

import java.io.IOException;
import java.util.*;
import java.text.ParseException;
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
    private TemplateSuratDb templateSuratDb;

    @Autowired
    private PenggunaService penggunaService;

    @GetMapping("/admin/detail/{id}/selesai")
    public String detailRequestSuratAdmin(@PathVariable("id") String id, Model model, Authentication auth)  throws IOException {
        RequestSurat requestSurats = requestService.getRequestSuratById(id);
        SuratKeluar suratKeluar = suratKeluarService.getFileTtd(id);
        byte[] pdf = suratKeluar.getFile();
        SuratKeluar suratKeluar1 = requestSurats.getSurat();
        String base64PDF = Base64.getEncoder().encodeToString(pdf);

        model.addAttribute("requestSurats", requestSurats);
        model.addAttribute("outgoing", suratKeluar1);
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

        if (requestSurats.getJenisSurat().equals("Lainnya")) {
            RequestTemplate fileExample = requestService.getFile(id);
            if (fileExample != null) {
                byte[] pdfEx = fileExample.getFile();
                if (pdfEx != null) {
                    String base64PDFEx = Base64.getEncoder().encodeToString(pdfEx);
                    model.addAttribute("base64PDFEx", base64PDFEx);
                    model.addAttribute("template", fileExample);
                }
            } else {
                model.addAttribute("fileNotFoundMessage", "File tidak tersedia.");
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

        if (requestSurats.getJenisSurat().equals("Lainnya")) {
            RequestTemplate fileExample = requestService.getFile(id);
            TemplateSurat templateSurat = templateSuratDb.findByRequestTemplate(requestSurats.getId());
            model.addAttribute("template", templateSurat);

            if (fileExample != null) {
                byte[] pdf = fileExample.getFile();
                if (pdf != null) {
                    String base64PDFEx = Base64.getEncoder().encodeToString(pdf);
                    model.addAttribute("base64PDFEx", base64PDFEx);
                    model.addAttribute("template", fileExample);
                }
            } else {
                model.addAttribute("fileNotFoundMessage", "File tidak tersedia.");
            }

            model.addAttribute("template", templateSurat);

        } else {
            TemplateSurat templateSurat = templateSuratDb.findByNamaTemplate(requestSurats.getJenisSurat());
            model.addAttribute("template", templateSurat);
        }

        SuratKeluar file = suratKeluarService.getFileTtd(id);

        if (file != null) {
            byte[] pdf = file.getFile();

            if (pdf != null) {
                String base64PDF = Base64.getEncoder().encodeToString(pdf);
                model.addAttribute("base64PDF", base64PDF);
                model.addAttribute("template", file);
            }
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
    public String createSuratKeluar(@PathVariable("id") String requestSuratId, @RequestParam("file") MultipartFile file, @RequestParam("kategori") String kategori,
        @RequestParam("jenisSurat") String jenisSurat, @RequestParam(value = "penandatangan", required = false) ArrayList<String> penandatanganIds, Model model) {
        
        try {
            RequestSurat requestSurat = requestService.getRequestSuratById(requestSuratId);

            List<Pengguna> penandatangans = new ArrayList<>();
            if (penandatanganIds != null) {
                for (String penandatanganId : penandatanganIds) {
                    Pengguna pengguna = penggunaService.getPenggunaById(penandatanganId);
                    penandatangans.add(pengguna);
                }
            }

            SuratKeluar existingSuratKeluar = suratKeluarService.findSuratKeluarByRequestID(requestSuratId);
            if (existingSuratKeluar != null) {
                if (file != null && !file.isEmpty()) {
                    existingSuratKeluar.setFile(file.getBytes());
                    existingSuratKeluar.setFileName(StringUtils.cleanPath(file.getOriginalFilename()));
                }
                existingSuratKeluar.setPenandatangan(penandatangans);
                suratKeluarService.update(existingSuratKeluar); 
            } else {
                suratKeluarService.storeTtd(requestSurat, file, kategori, jenisSurat, penandatangans);
            }

            return "redirect:/admin/request/process";

        } catch (Exception e) {
            return "redirect:/admin/request/process";
        }
    }

    @GetMapping("/ttd/detail/{id}")
    @Transactional(readOnly = true)
    public String detailRequestSuratPengurus(@PathVariable("id") String id, Model model, Authentication auth)  throws IOException {
        RequestSurat requestSurats = requestService.getRequestSuratById(id);
        model.addAttribute("requestSurats", requestSurats);

        SuratKeluar suratKeluar = suratKeluarService.getFileTtd(id);
        byte[] pdf = suratKeluar.getFile();

        SuratKeluar suratKeluar1 = requestSurats.getSurat();
        model.addAttribute("outgoing", suratKeluar1);

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
                model.addAttribute("user", pengguna);
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
    public String updateTTD(@PathVariable("id") String id, @RequestParam("file") MultipartFile file, Model model, Authentication auth) {
        String message = "";

        try {
            suratKeluarService.updateSuratKeluarFile(id, file);
            message = "PDF updated successfully";
            model.addAttribute("message", message);
            return "redirect:/ttd/detail/{id}";
        } catch (Exception e) {
            message = "Failed to update the template: " + e.getMessage();
            model.addAttribute("errorMessage", message);
            return "redirect:/ttd/detail/{id}";
        }
    }

    @GetMapping("/ttd/request")
    @Transactional(readOnly = true)
    public String showAllRequestsTTD(Model model, Authentication auth) {
        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                String penandatanganId = pengguna.getId();
                List<RequestSurat> requestSurats;
                requestSurats = requestService.getAllRequestSuratByPenandatanganId(penandatanganId);

                requestSurats.sort(Comparator.comparingInt(RequestSurat::getStatus));
                model.addAttribute("requestSurats", requestSurats);
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        return "pengurus-ttd-request";
    }

    @GetMapping("/ttd/arsip")
    @Transactional(readOnly = true)
    public String pengurusTtdArsip(Model model, Authentication auth) {
        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                List<SuratKeluar> listSuratKeluar;
                listSuratKeluar = suratKeluarService.getSuratKeluarByCurrentPenandatangan(pengguna);

                model.addAttribute("listSuratKeluar", listSuratKeluar);
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }
        return "pengurus-ttd-arsip";
    }

    @GetMapping("/surat-keluar/all")
    public String getAllSuratKeluar(Model model, Authentication auth) { 
        List<SuratKeluar> suratKeluarList = suratKeluarService.getSuratKeluarByIsSigned(true);
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

    @GetMapping("/surat-keluar/form")
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

    @PostMapping("/surat-keluar/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("kategori") String kategori,  
        @RequestParam("perihal") String perihal, @RequestParam("penerima_eksternal") String penerima_eksternal, 
        Authentication auth, Model model) throws ParseException {

        try {
            SuratKeluar suratKeluar= suratKeluarService.storeInput(file, kategori, perihal, penerima_eksternal);
            suratKeluar.setIsSigned(true);
            suratKeluarDb.save(suratKeluar);
            return "redirect:/surat-keluar/detail/" + suratKeluar.getNomorArsip();

        } catch (Exception e) {
            return "redirect:/surat-keluar/form";
        }
    }

    @GetMapping("/surat-keluar/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String id, Authentication auth) {
        SuratKeluar file = suratKeluarService.getFileInput(id);

        if (file != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", file.getFileName());

            return ResponseEntity.ok().headers(headers).body(file.getFile());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/surat-keluar/detail/{id}")
    public String previewPDF(@PathVariable("id") String id, Model model, Authentication auth) throws IOException {
        SuratKeluar suratKeluar = suratKeluarService.getFileInput(id);
        byte[] pdf = suratKeluar.getFile();

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

        return "detail-surat-keluar"; 
    }

    @GetMapping("/ttd/followup/{id}")
    public String detailTtdFollowUp(@PathVariable("id") String id, Model model, Authentication auth)  throws IOException {
        SuratKeluar suratKeluar = suratKeluarService.getSuratKeluarByNomorArsip(id);
        byte[] pdf = suratKeluar.getFile();
        String base64PDF = Base64.getEncoder().encodeToString(pdf);

        model.addAttribute("suratKeluar", suratKeluar);
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
                model.addAttribute("user", pengguna);
            } else {
                return "auth-failed";
            }
        }

        return "pengurus-detail-followup"; 
    }

    @PostMapping("/ttd/followup/{id}")
    public String updateTtdFollowUp(@PathVariable("id") String id, @RequestParam("file") MultipartFile file, Model model, Authentication auth) {
        String message = "";

        try {
            suratKeluarService.updateFollowUpFile(id, file);
            message = "PDF updated successfully";
            model.addAttribute("message", message);
            return "redirect:/ttd/followup/{id}";
        } catch (Exception e) {
            message = "Failed to update the template: " + e.getMessage();
            model.addAttribute("errorMessage", message);
            return "redirect:/ttd/followup/{id}";
        }
    }
}
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
import java.text.ParseException;

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

        SuratKeluar suratKeluar = suratKeluarService.getFileTtd(id);
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

        SuratKeluar file = suratKeluarService.getFileTtd(id);

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
                                    @RequestParam(value = "penandatangan", required = false) ArrayList<String> penandatanganIds,
                                    Model model) {
        try {
            System.out.println("msk controller");
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
                suratKeluarService.update(existingSuratKeluar); // Update existing SuratKeluar
            } else {
                // Create and store SuratKeluar
                suratKeluarService.storeTtd(requestSurat, file, kategori, jenisSurat, penandatangans);
            }

            return "redirect:/admin/request/process";
        } catch (Exception e) {
            System.out.println("error:" + e.getMessage());
            return "redirect:/admin/request/process";
        }
    }


    // @GetMapping("/pengurus/request")
    // public String showAllRequestsPengurus(Model model, Authentication auth) {
    //     List<RequestSurat> requestSurats = requestService.getAllOnProcessRequestsSurat();
    //     model.addAttribute("requestSurats", requestSurats);

    //     if (auth != null) {
    //         OidcUser oauthUser = (OidcUser) auth.getPrincipal();
    //         String email = oauthUser.getEmail();
    //         Optional<Pengguna> user = penggunaDb.findByEmail(email);

    //         if (user.isPresent()) {
    //             Pengguna pengguna = user.get();
    //             model.addAttribute("role", penggunaService.getRole(pengguna));
    //             model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
    //         } else {
    //             return "auth-failed";
    //         }
    //     }

    //     return "pengurus-ttd-request";
    // }

    @GetMapping("/ttd/detail/{id}")
    public String detailRequestSuratPengurus(@PathVariable("id") String id, Model model, Authentication auth)  throws IOException {
        RequestSurat requestSurats = requestService.getRequestSuratById(id);
        model.addAttribute("requestSurats", requestSurats);

        SuratKeluar suratKeluar = suratKeluarService.getFileTtd(id);
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
            return "redirect:/ttd/detail/{id}";
        } catch (Exception e) {
            message = "Failed to update the template: " + e.getMessage();
            System.out.println(message);
            model.addAttribute("errorMessage", message);
            return "redirect:/ttd/detail/{id}";
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
    // route to pengurus-ttd-arsip 
    @GetMapping("/ttd/arsip")
    @Transactional(readOnly = true)
    public String pengurusTtdArsip( Model model, Authentication auth) {
        // list surat keluar berdasarkan id penandatangan dan status
        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                List<SuratKeluar> listSuratKeluar = suratKeluarService.getSuratKeluarByPenandatanganAndIsSigned(pengguna, false);
                model.addAttribute("listSuratKeluar", listSuratKeluar);
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }
        return "pengurus-ttd-arsip";
    }
    


    /* BRANCH ARSIPPP
     * BRANCH ARSIPPP
     * BRANCH ARSIPPP
     * BRANCH ARSIPPP
     * BRANCH ARSIPPP
    */
    //get all surat keluar (diarsipkan)
    @GetMapping("/surat-keluar/all")
    public String getAllSuratKeluar(Model model, Authentication auth,
        @RequestParam(value = "tanggalDibuat", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date tanggalDibuat) {
        List<SuratKeluar> suratKeluarList = suratKeluarService.getSuratKeluarByIsSigned(true);
        
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

    // route to form-upload-surat
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
            return "redirect:/surat-keluar/detail/" + suratKeluar.getNomorArsip();
        }catch (Exception e) {
            //debug
            System.out.println("error kenapee:" + e.getMessage());
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

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(file.getFile());
        } else {
            // Handle the case where file is not found
            return ResponseEntity.notFound().build();
        }
    }

    // Metode untuk menampilkan preview PDF
    @GetMapping("/surat-keluar/detail/{id}")
    public String previewPDF(@PathVariable("id") String id, Model model, Authentication auth) throws IOException {
        SuratKeluar suratKeluar = suratKeluarService.getFileInput(id);
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

    @GetMapping("/surat-keluar/daftar")
    public String form(Model model) {
        return "daftar-surat-keluar";
    }

    // route to semua-surat-keluar
    @GetMapping("/surat-keluar/daftar-arsip")
    public String semuaSuratKeluar(Model model, Authentication auth) {
        return "daftar-arsip-tes";
    }

    // route to detail-surat-keluar
    @GetMapping("/surat-keluar/detail-surat-keluar")
    public String detailSuratKeluar(Model model, Authentication auth) {
        return "detail-surat-keluar";
    }

    @GetMapping("/surat-keluar/search")
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

    // route to pengurus-ttd-arsip


}
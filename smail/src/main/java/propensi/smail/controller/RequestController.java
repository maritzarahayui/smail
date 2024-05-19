package propensi.smail.controller;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

import io.jsonwebtoken.lang.Arrays;

import propensi.smail.model.*;
import propensi.smail.model.user.*;
import propensi.smail.dto.RequestAndFieldDataDTO;
import propensi.smail.repository.*;
import propensi.smail.service.*;

import java.text.*;
import java.util.*;
import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.ObjectError;

@Controller
public class RequestController {
    
    @Autowired
    private RequestService requestService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    PenggunaDb penggunaDb;

    @Autowired
    PenggunaService penggunaService;

    @Autowired
    RequestSuratDb requestSuratDb;

    @Autowired
    RequestTemplateDb requestTemplateDb;

    @Autowired
    private SuratKeluarService suratKeluarService;

    @GetMapping("/request")
    public String formRequestSurat(Model model, Authentication auth){

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

        // DTO untuk wrap data gabungan antara request surat - request template - field data
        model.addAttribute("requestDTO", new RequestAndFieldDataDTO());

        Map<Integer, String> listBentukSurat = requestService.listBentukSurat();
        model.addAttribute("listBentukSurat", listBentukSurat);

        Map<Integer, String> listBahasa = requestService.listBahasa();
        model.addAttribute("listBahasa", listBahasa);
       
        Map<String, List<String>> jenisSuratByKategori = requestService.generateJenisSuratByKategoriAndRole(role);
        model.addAttribute("jenisSuratByKategori", jenisSuratByKategori);

        Map<Integer, String> listKategori = requestService.listKategori();
        model.addAttribute("listKategori", listKategori);

        model.addAttribute("requestTemplate", new RequestTemplate());
       
        model.addAttribute("requestSurat", new RequestSurat());

        List<TemplateSurat> listTemplateSurat = templateService.getAllActiveTemplates();
        model.addAttribute("listTemplateSurat", listTemplateSurat);

        return "request-surat";
    }

    @PostMapping("/request")
    public String requestSurat(@Valid @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ModelAttribute RequestAndFieldDataDTO requestDTO,
                                @RequestParam(value = "file", required = false) MultipartFile file,            
                                BindingResult bindingResult, Authentication auth) throws IOException {
        if (bindingResult.hasErrors()) {
            return bindingResult.getAllErrors().toString();
        }

        OidcUser oauthUser = (OidcUser) auth.getPrincipal();
        String email = oauthUser.getEmail();
        Optional<Pengguna> user = penggunaDb.findByEmail(email);
        Pengguna pengguna = null;

        if (user.isPresent()) { 
            pengguna = user.get(); 
        }
        
        if (requestDTO.getJenisSurat().equals("Lainnya")) {
            RequestTemplate requestTemplate = new RequestTemplate();

            if (file != null && !file.isEmpty()) {
                String fileName = StringUtils.cleanPath(file.getOriginalFilename());
                requestTemplate.setFile(file.getBytes());
                requestTemplate.setFileName(fileName);
            }
            requestTemplate.setPengaju(pengguna);
            requestTemplate.setTanggalPengajuan(new Date());
            requestService.createRequestTemplate(requestTemplate,requestDTO);
        } 
        
        try {
            RequestSurat requestSurat = new RequestSurat();
            requestSurat.setPengaju(pengguna);
            requestSurat.setTanggalPengajuan(new Date());
            requestService.createRequestSurat(requestSurat, requestDTO);
            List<String> bentukSurat = requestDTO.getBentukSurat();
            requestSurat.setBentukSurat(bentukSurat);
            return "redirect:/request/history";
        } catch (Exception e) {
            e.printStackTrace();
            return "Gagal membuat permintaan surat: " + e.getMessage(); 
        }
    }

    @GetMapping("/request/history")
    @Transactional(readOnly = true)
    public String showAllRequests(Model model, Authentication auth) {
        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                String userId = pengguna.getId();

                // Get all request types
                List<RequestSurat> allRequests = requestService.getAllSubmittedRequestsSuratByPengaju(userId);
                List<RequestSurat> cancelledRequests = requestService.getAllCancelledRequestsSuratByPengaju(userId);
                List<RequestSurat> rejectedRequests = requestService.getAllRejectedRequestsSuratByPengaju(userId);
                List<RequestSurat> onProcessRequests = requestService.getAllOnProcessRequestsSuratByPengaju(userId);
                List<RequestSurat> finishedRequests = requestService.getAllFinishedRequestsSuratByPengaju(userId);

                // Get all surat keluar
                List<SuratKeluar> suratKeluar = suratKeluarService.getAllSuratKeluar();

                model.addAttribute("allRequests", allRequests);
                model.addAttribute("cancelledRequests", cancelledRequests);
                model.addAttribute("rejectedRequests", rejectedRequests);
                model.addAttribute("onProcessRequests", onProcessRequests);
                model.addAttribute("finishedRequests", finishedRequests);
                model.addAttribute("suratKeluar", suratKeluar);
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));

                return "user-history";
            } else {
                return "auth-failed";
            }
        }

        return "user-history";
    }

    @PostMapping("/request/history/{id}/cancel")
    public String cancelRequest(Model model, Authentication auth, @PathVariable("id") String id,  @RequestParam(value = "alasanPembatalan", required = false) String alasanPembatalan) {
        requestService.batalkanRequestSurat(id, alasanPembatalan);
        return "redirect:/detail/"+id+"/request";
    }

    @GetMapping("/request/history/search")
    @Transactional(readOnly = true)
    public String searchHistory(@RequestParam(name = "keyword", required = false) String keyword,
                                Model model, Authentication auth) {

        String pengaju = "";
        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                pengaju = pengguna.getId();
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        List<RequestSurat> allRequests = new ArrayList<>();
        List<RequestSurat> cancelledRequests = new ArrayList<>();
        List<RequestSurat> rejectedRequests = new ArrayList<>();
        List<RequestSurat> onProcessRequests = new ArrayList<>();
        List<RequestSurat> finishedRequests = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            allRequests = requestSuratDb.findByKeyword(keyword);
            cancelledRequests = requestService.searchRequests(keyword, 2);
            rejectedRequests = requestService.searchRequests(keyword, 3);
            onProcessRequests = requestService.searchRequests(keyword, 4);
            finishedRequests = requestService.searchRequests(keyword, 5);
        } else {
            allRequests = requestService.getAllSubmittedRequestsSuratByPengaju(pengaju);
            cancelledRequests = requestService.getAllCancelledRequestsSuratByPengaju(pengaju);
            rejectedRequests = requestService.getAllRejectedRequestsSuratByPengaju(pengaju);
            onProcessRequests = requestService.getAllOnProcessRequestsSuratByPengaju(pengaju);
            finishedRequests = requestService.getAllFinishedRequestsSuratByPengaju(pengaju);
        }

        model.addAttribute("allRequests", allRequests);
        model.addAttribute("cancelledRequests", cancelledRequests);
        model.addAttribute("rejectedRequests", rejectedRequests);
        model.addAttribute("onProcessRequests", onProcessRequests);
        model.addAttribute("finishedRequests", finishedRequests);

        if (allRequests.size() == 0 || cancelledRequests.size() == 0 || rejectedRequests.size() == 0 ||
            onProcessRequests.size() == 0 || finishedRequests.size() == 0) {
            model.addAttribute("message", "Tidak ada data yang cocok dengan pencarian Anda.");
        }

        return "user-history";
    }

    // Admin
    @GetMapping("/admin/request/{id}")
    public String showDetaileRequests(@PathVariable("id") String id, Model model, Authentication auth) {
        RequestSurat req = requestService.getRequestSuratById(id);

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

        model.addAttribute("requestSurat", req); // Add the template object to the model
        return "detail-request-admin";
    }

    @PostMapping("/admin/request/updateStatus")
    public String updateRequestStatus(@RequestParam("requestId") String requestId, @RequestParam("status") int status) {
        // Retrieve the request by ID
        RequestSurat req = requestService.getRequestSuratById(requestId);

        // Update the status
        req.setStatus(status);
        requestService.saveOrUpdate(req); // Assuming there's a method to save/update the request

        // Redirect to the detail page of the request
        return "redirect:/admin/request/" + requestId;
    }

    @GetMapping("/detail/{id}/request")
    @Transactional(readOnly = true)
    public String detailRequestSurat(@PathVariable("id") String id, Model model, Authentication auth) {
        RequestSurat requestSurats = requestService.getRequestSuratById(id);
        
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
                byte[] pdf = fileExample.getFile();
                if (pdf != null) {
                    String base64PDFEx = Base64.getEncoder().encodeToString(pdf);
                    model.addAttribute("base64PDFEx", base64PDFEx);
                    model.addAttribute("template", fileExample);
                }
            } else {
                model.addAttribute("fileNotFoundMessage", "File tidak tersedia.");
            }
        }

        SuratKeluar suratKeluar1 = requestSurats.getSurat();
        model.addAttribute("outgoing", suratKeluar1);

        SuratKeluar file = suratKeluarService.getFileTtd(id);

        if (file != null) {
            byte[] pdf = file.getFile();

            // Convert PDF content to Base64
            String base64PDF = Base64.getEncoder().encodeToString(pdf);

            model.addAttribute("base64PDF", base64PDF);
            model.addAttribute("template", file);
        }

        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(1, "Diajukan");
        statusMap.put(2, "Dibatalkan");
        statusMap.put(3, "Ditolak");
        statusMap.put(4, "Diproses");
        statusMap.put(5, "Selesai");

        model.addAttribute("statusMap", statusMap);
        model.addAttribute("requestSurats", requestSurats);

        return "user-history-detail"; 
    }

    @GetMapping("/admin/request/all")
    @Transactional(readOnly = true)
    public String showAllRequests(@RequestParam(name = "keyword", required = false) String keyword, Model model, Authentication auth) {
        List<RequestSurat> requestSurats;

        if (keyword != null && !keyword.isEmpty()) {
//            requestSurats = requestService.searchRequests(keyword, 1);
            requestSurats = requestService.getAllRequestsSurat();
        } else {
            requestSurats = requestService.getAllRequestsSurat();
        }
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

        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(1, "Diajukan");
        statusMap.put(2, "Dibatalkan");
        statusMap.put(3, "Ditolak");
        statusMap.put(4, "Diproses");
        statusMap.put(5, "Selesai");

        model.addAttribute("statusMap", statusMap);

        return "admin-history-all";
    }

    @GetMapping("/admin/request")
    @Transactional(readOnly = true)
    public String showAllRequestsAdmin(@RequestParam(name = "keyword", required = false) String keyword, Model model, Authentication auth) {
        List<RequestSurat> requestSurats;

        if (keyword != null && !keyword.isEmpty()) {
            requestSurats = requestService.searchRequests(keyword, 1);
        } else {
            requestSurats = requestService.getAllSubmitedRequestsSurat();
        }
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

        return "admin-history-diajukan";
    }

    @GetMapping("/admin/request/cancelled")
    @Transactional(readOnly = true)
    public String showAllCancelledRequestsAdmin(@RequestParam(name = "keyword", required = false) String keyword, Model model, Authentication auth) {
        List<RequestSurat> requestSurats;

        if (keyword != null && !keyword.isEmpty()) {
            requestSurats = requestService.searchRequests(keyword, 2);
        } else {
            requestSurats = requestService.getAllCanceledRequestsSurat();
        }
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

        return "admin-history-dibatalkan";
    }

    @GetMapping("/admin/request/rejected")
    @Transactional(readOnly = true)
    public String showAllRejectedRequestsAdmin(@RequestParam(name = "keyword", required = false) String keyword, Model model, Authentication auth) {
        List<RequestSurat> requestSurats;

        if (keyword != null && !keyword.isEmpty()) {
            requestSurats = requestService.searchRequests(keyword, 3);
        } else {
            requestSurats = requestService.getAllRejectedRequestsSurat();
        }
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

        return "admin-history-ditolak";
    }

    @GetMapping("/admin/request/process")
    @Transactional(readOnly = true)
    public String showAllProcessingRequestsAdmin(@RequestParam(name = "keyword", required = false) String keyword, Model model, Authentication auth) {
        try {
            List<RequestSurat> requestSurats;
            if (keyword != null && !keyword.isEmpty()) {
                requestSurats = requestService.searchRequests(keyword, 4);
            } else {
                requestSurats = requestService.getAllOnProcessRequestsSurat();
            }
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

            return "admin-history-diproses";
        } catch (Exception e) {
            // Handle exception
            return "error-page";
        }
    }

    @GetMapping("/admin/request/finished")
    @Transactional(readOnly = true)
    public String showAllFinishedRequestsAdmin(@RequestParam(name = "keyword", required = false) String keyword, Model model, Authentication auth) {
        List<RequestSurat> requestSurats;

        if (keyword != null && !keyword.isEmpty()) {
            requestSurats = requestService.searchRequests(keyword, 5);
        } else {
            requestSurats = requestService.getAllFinishedRequestsSurat();
        }
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

        return "admin-history-selesai";
    }

    @GetMapping("/admin/detail/{id}")
    @Transactional(readOnly = true)
    public String detailRequestSuratAdmin(@PathVariable("id") String id, Model model, Authentication auth) {
        RequestSurat requestSurats = requestService.getRequestSuratById(id);
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

        if (requestSurats.getJenisSurat().equals("Lainnya")) {
            RequestTemplate file = requestService.getFile(id);
            if (file != null) {
                byte[] pdf = file.getFile();
                if (pdf != null) {
                    String base64PDF = Base64.getEncoder().encodeToString(pdf);
                    model.addAttribute("base64PDF", base64PDF);
                    model.addAttribute("template", file);
                }
            } else {
                model.addAttribute("fileNotFoundMessage", "File tidak tersedia.");
            }
        }

        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(1, "Diajukan");
        statusMap.put(3, "Ditolak");
        statusMap.put(4, "Diproses");

        model.addAttribute("statusMap", statusMap);

        List<Pengguna> listTembusan = penggunaDb.findAll().stream()
                .filter(user -> {
                    String role = penggunaService.getRole(user);
                    return role.equals("Dosen") || role.equals("Pengurus");
                })
                .collect(Collectors.toList());

        model.addAttribute("listTembusan", listTembusan);

        return "admin-detail-diajukan";
    }

    @GetMapping("/admin/detail/{id}/cancelled")
    @Transactional(readOnly = true)
    public String detailRequestSuratAdminCancelled(@PathVariable("id") String id, Model model, Authentication auth) {
        RequestSurat requestSurats = requestService.getRequestSuratById(id);
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

        if (requestSurats.getJenisSurat().equals("Lainnya")) {
            RequestTemplate file = requestService.getFile(id);
            if (file != null) {
                byte[] pdf = file.getFile();
                if (pdf != null) {
                    String base64PDF = Base64.getEncoder().encodeToString(pdf);
                    model.addAttribute("base64PDF", base64PDF);
                    model.addAttribute("template", file);
                }
            } else {
                model.addAttribute("fileNotFoundMessage", "File tidak tersedia.");
            }
        }

        return "admin-detail-dibatalkan"; 
    }

    @GetMapping("/admin/detail/{id}/rejected")
    @Transactional(readOnly = true)
    public String detailRequestSuratAdminTolak(@PathVariable("id") String id, Model model, Authentication auth) {
        RequestSurat requestSurats = requestService.getRequestSuratById(id);
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

        if (requestSurats.getJenisSurat().equals("Lainnya")) {
            RequestTemplate file = requestService.getFile(id);
            if (file != null) {
                byte[] pdf = file.getFile();
                if (pdf != null) {
                    String base64PDF = Base64.getEncoder().encodeToString(pdf);
                    model.addAttribute("base64PDF", base64PDF);
                    model.addAttribute("template", file);
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

        return "admin-detail-ditolak";
    }

    @PostMapping("/admin/detail/{id}/updateStatus")
    public String updateStatus(@PathVariable("id") String id, @RequestParam("status") int status, @RequestParam(value = "alasanPenolakan", required = false) String alasanPenolakan,
                               Model model, Authentication auth) throws MessagingException, IOException {
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
        
        RequestSurat requestSurat = requestService.getRequestSuratById(id);
        requestSurat.setStatus(status);

        if (requestSurat.getJenisSurat().equals("Lainnya")) {
            RequestTemplate requestTemplate = requestService.getRequestTemplateById(id);
            if (status == 3) {
                requestTemplate.setStatus(3);
                requestTemplate.setAlasanPenolakan(alasanPenolakan);
            } else if (status == 4) {
                requestTemplate.setStatus(2);
            }
            requestTemplateDb.save(requestTemplate);
        }

        if (status == 3) {
            requestSurat.setAlasanPenolakan(alasanPenolakan);
            requestSurat.setTanggalPenolakan(new Date());

            requestSuratDb.save(requestSurat);

            requestService.sendEmailRejection(requestSurat.getPengaju().getEmail(), "", "", requestSurat);
        } else {
            requestSurat.setAlasanPenolakan(null);
            requestSuratDb.save(requestSurat);
        }

        String redirectUrl = "/admin/detail/" + id;
        switch (status) {
            case 3:
                redirectUrl = "/admin/request/rejected";
                break;
            case 4:
                redirectUrl = "/admin/request/process";
                break;
            default:
        }

        return "redirect:" + redirectUrl;
    }
}

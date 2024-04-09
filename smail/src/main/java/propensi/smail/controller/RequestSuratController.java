package propensi.smail.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import propensi.smail.model.user.*;
import propensi.smail.dto.RequestAndFieldDataDTO;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.TemplateSurat;
import propensi.smail.repository.PenggunaDb;
import propensi.smail.repository.RequestSuratDb;
import propensi.smail.model.RequestTemplate;
import propensi.smail.service.PenggunaService;
import propensi.smail.service.RequestService;
import propensi.smail.service.TemplateService;

import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import io.jsonwebtoken.lang.Arrays;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.validation.ObjectError;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Controller
// @RestController
// @RequestMapping("/api")
public class RequestSuratController {
    
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
                                    BindingResult bindingResult, Authentication auth) {
        if (bindingResult.hasErrors()) {
            return bindingResult.getAllErrors().toString();
        }

        System.out.println(requestDTO.getJenisSurat() + "    PPPPPPPHAHHAHAHHAH!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println(requestDTO.getListFieldData().toString());

        OidcUser oauthUser = (OidcUser) auth.getPrincipal();
        String email = oauthUser.getEmail();
        Optional<Pengguna> user = penggunaDb.findByEmail(email);
        Pengguna pengguna = null;

        if (user.isPresent()) { 
            pengguna = user.get(); 
        }
        
        if (requestDTO.getJenisSurat().equals("Lainnya")) {
            RequestTemplate requestTemplate = new RequestTemplate();
            requestTemplate.setPengaju(pengguna);
            requestTemplate.setTanggalPengajuan(new Date());
            requestService.createRequestTemplate(requestTemplate,requestDTO);
        } 
        
        try {
            RequestSurat requestSurat = new RequestSurat();
            requestSurat.setPengaju(pengguna);
            requestSurat.setTanggalPengajuan(new Date());
            requestService.createRequestSurat(requestSurat, requestDTO);

            System.out.println("BERHASIL");
            return "redirect:/request/history";
        } catch (Exception e) {
            e.printStackTrace();
            return "Gagal membuat permintaan surat: " + e.getMessage(); 
        }
        
        
    }
    
    @GetMapping("all")
    public ResponseEntity<List<RequestSurat>> showAllRequest() {
        List<RequestSurat> allRequests = requestService.getAllRequestsSurat();
        return new ResponseEntity<>(allRequests, HttpStatus.OK);
    }

    @GetMapping("/request/history")
    public String showAllRequests(Model model, Authentication auth) {
        List<RequestSurat> requestSurats = requestService.getAllSubmitedRequestsSurat();
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

        return "user-history-diajukan";
    }

    @GetMapping("/request/history/cancelled")
    public String showCancelledRequests(Model model, Authentication auth) {
        List<RequestSurat> requestSurats = requestService.getAllCanceledRequestsSurat();
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

        return "user-history-dibatalkan";
    }

    @GetMapping("/request/history/rejected")
    public String showRejectedRequests(Model model, Authentication auth) {
        List<RequestSurat> requestSurats = requestService.getAllRejectedRequestsSurat();
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

        return "user-history-ditolak";
    }

    @GetMapping("/request/history/process")
    public String showOnProcessRequests(Model model, Authentication auth) {
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

        return "user-history-diproses";
    }

    @GetMapping("/request/history/finished")
    public String showFinishedRequests(Model model, Authentication auth) {
        List<RequestSurat> requestSurats = requestService.getAllFinishedRequestsSurat();
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

        return "user-history-selesai";
    }

    @GetMapping("/{requestSuratId}")
    public ResponseEntity<RequestSurat> showDetailRequest(@PathVariable("requestSuratId") String requestSuratId) {
        try {
            RequestSurat requestSurat = requestService.getRequestSuratById(requestSuratId);
            return new ResponseEntity<>(requestSurat, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RequestSurat with id: " + requestSuratId + " not found", e);
        }
    }

    @GetMapping("/detail/{id}")
    public String detailRequestSurat(@PathVariable("id") String id, Model model, Authentication auth) {
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

        return "user-detail-diajukan"; 
    }

    @PutMapping("/{requestSuratId}/cancel")
    public ResponseEntity<RequestSurat> cancelRequest(@PathVariable("requestSuratId") String requestSuratId, @RequestParam("reason") String reason) {
        try {
            RequestSurat canceledRequestSurat = requestService.batalkanRequestSurat(requestSuratId, reason);
            return new ResponseEntity<>(canceledRequestSurat, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RequestSurat with id: " + requestSuratId + " not found", e);
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelRequestSurat(@PathVariable("id") String id, @RequestParam("reason") String reason, Model model, Authentication auth) {
        RequestSurat requestSurats = requestService.batalkanRequestSurat(id, reason);
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

        return "user-detail-dibatalkan"; 
    }

    @GetMapping("/admin/request")
    public String showAllRequestsAdmin(Model model, Authentication auth) {
        List<RequestSurat> requestSurats = requestService.getAllSubmitedRequestsSurat();
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
    public String showAllCancelledRequestsAdmin(Model model, Authentication auth) {
        List<RequestSurat> requestSurats = requestService.getAllCanceledRequestsSurat();
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
    public String showAllRejectedRequestsAdmin(Model model, Authentication auth) {
        List<RequestSurat> requestSurats = requestService.getAllRejectedRequestsSurat();
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
    public String showAllProcessingRequestsAdmin(Model model, Authentication auth) {
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

        return "admin-history-diproses";
    }

    @GetMapping("/admin/request/finished")
    public String showAllFinishedRequestsAdmin(Model model, Authentication auth) {
        List<RequestSurat> requestSurats = requestService.getAllFinishedRequestsSurat();
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

        model.addAttribute("listTembusan", listTembusan);
        
        return "admin-detail-diajukan"; 
    }

    @PostMapping("/admin/detail/{id}/updateStatus")
    public String updateStatus(@PathVariable("id") String id, @RequestParam("status") int status, Model model, Authentication auth) {
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
        requestSuratDb.save(requestSurat);

        String redirectUrl = "/admin/detail/" + id;
        switch (status) {
            case 3:
                redirectUrl = "/admin/request/rejected";
                break;
            case 4:
                redirectUrl = "/admin/request/process";
                break;
            case 5:
                redirectUrl = "/admin/request/finished";
                break;
            default:
        }

        return "redirect:" + redirectUrl;
    }
}

package propensi.smail.controller;

import jakarta.mail.MessagingException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import propensi.smail.model.user.*;
import propensi.smail.dto.RequestAndFieldDataDTO;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.TemplateSurat;
import propensi.smail.repository.PenggunaDb;
import propensi.smail.repository.RequestSuratDb;
import propensi.smail.model.RequestTemplate;
import propensi.smail.model.SuratKeluar;
import propensi.smail.model.SuratMasuk;
import propensi.smail.service.PenggunaService;
import propensi.smail.service.RequestService;
import propensi.smail.service.SuratKeluarService;
import propensi.smail.service.TemplateService;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Base64;
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
import org.springframework.util.StringUtils;
import org.springframework.validation.ObjectError;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Controller
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
                                @RequestParam("file") MultipartFile file,            
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

            String fileName = StringUtils.cleanPath(file.getOriginalFilename());

            requestTemplate.setFile(file.getBytes());
            requestTemplate.setFileName(fileName);
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
            System.out.println("BERHASIL");
            return "redirect:/request/history";
        } catch (Exception e) {
            e.printStackTrace();
            return "Gagal membuat permintaan surat: " + e.getMessage(); 
        }
    }

    @GetMapping("/request/history")
    public String showAllRequests(Model model, Authentication auth) {
        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                String userId = pengguna.getId();
                List<RequestSurat> requestSurats = requestService.getAllSubmittedRequestsSuratByPengaju(userId);
                model.addAttribute("requestSurats", requestSurats);
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        return "user-history-diajukan";
    }

    @GetMapping("/request/history/{id}/cancel")
    public String cancelRequest(Model model, Authentication auth, @PathVariable("id") String id) {
        requestService.batalkanRequestSurat(id, "test");

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                String userId = pengguna.getId();
                List<RequestSurat> requestSurats = requestService.getAllSubmittedRequestsSuratByPengaju(userId);
                model.addAttribute("requestSurats", requestSurats);
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        return "redirect:/detail/"+id+"/cancelled";
    }

    @GetMapping("/request/history/search")
    @Transactional(readOnly = true)
    public String searchHistory(@RequestParam(name = "search", required = false) String search,
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

        List<RequestSurat> requestSurats; 
        if (search != null && !search.isEmpty()) {
            requestSurats = requestService.getBySearchAndStatusAndPengaju(1, search, pengaju);
            model.addAttribute("requestSurats", requestSurats);
        } else {
            // Jika tidak ada input, kembalikan semua permintaan surat
            requestSurats = requestService.getAllSubmittedRequestsSuratByPengaju(pengaju);
            model.addAttribute("requestSurats", requestSurats);
        }
        
        if (requestSurats.size() == 0) {
            model.addAttribute("message", "Tidak ada data yang cocok dengan pencarian Anda.");
        }

        return "user-history-diajukan";
    }

    @GetMapping("/request/history/cancelled")
    @Transactional(readOnly = true)
    public String showCancelledRequests(Model model, Authentication auth) {
        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                String userId = pengguna.getId();
                List<RequestSurat> requestSurats = requestService.getAllCancelledRequestsSuratByPengaju(userId);
                model.addAttribute("requestSurats", requestSurats);
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        return "user-history-dibatalkan";
    }

    @GetMapping("/request/history/cancelled/search")
    @Transactional(readOnly = true)
    public String searchHistoryCancelled(@RequestParam(name = "search", required = false) String search,
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

        List<RequestSurat> requestSurats; 
        if (search != null && !search.isEmpty()) {
            requestSurats = requestService.getBySearchAndStatusAndPengaju(2, search, pengaju);
            model.addAttribute("requestSurats", requestSurats);
        } else {
            // Jika tidak ada input, kembalikan semua permintaan surat
            requestSurats = requestService.getAllSubmittedRequestsSuratByPengaju(pengaju);
            model.addAttribute("requestSurats", requestSurats);
        }
        
        if (requestSurats.size() == 0) {
            model.addAttribute("message", "Tidak ada data yang cocok dengan pencarian Anda.");
        }           

        return "user-history-dibatalkan";
    }

    @GetMapping("/request/history/rejected")
    @Transactional(readOnly = true)
    public String showRejectedRequests(Model model, Authentication auth) {
        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                String userId = pengguna.getId();
                List<RequestSurat> requestSurats = requestService.getAllRejectedRequestsSuratByPengaju(userId);
                model.addAttribute("requestSurats", requestSurats);
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        return "user-history-ditolak";
    }

    @GetMapping("/request/history/rejected/search")
    @Transactional(readOnly = true)
    public String searchHistoryRejected(@RequestParam(name = "search", required = false) String search,
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

        List<RequestSurat> requestSurats; 
        if (search != null && !search.isEmpty()) {
            requestSurats = requestService.getBySearchAndStatusAndPengaju(3, search, pengaju);
            model.addAttribute("requestSurats", requestSurats);
        } else {
            // Jika tidak ada input, kembalikan semua permintaan surat
            requestSurats = requestService.getAllSubmittedRequestsSuratByPengaju(pengaju);
            model.addAttribute("requestSurats", requestSurats);
        }
        
        if (requestSurats.size() == 0) {
            model.addAttribute("message", "Tidak ada data yang cocok dengan pencarian Anda.");
        }
                            
        return "user-history-ditolak";
    }

    @GetMapping("/request/history/process")
    @Transactional(readOnly = true)
    public String showOnProcessRequests(Model model, Authentication auth) {
        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                String userId = pengguna.getId();
                List<RequestSurat> requestSurats = requestService.getAllOnProcessRequestsSuratByPengaju(userId);
                model.addAttribute("requestSurats", requestSurats);
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        return "user-history-diproses";
    }

    @GetMapping("/request/history/process/search")
    @Transactional(readOnly = true)
    public String searchHistoryOnProcess(@RequestParam(name = "search", required = false) String search,
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

        List<RequestSurat> requestSurats; 
        if (search != null && !search.isEmpty()) {
            requestSurats = requestService.getBySearchAndStatusAndPengaju(4, search, pengaju);
            model.addAttribute("requestSurats", requestSurats);
        } else {
            // Jika tidak ada input, kembalikan semua permintaan surat
            requestSurats = requestService.getAllSubmittedRequestsSuratByPengaju(pengaju);
            model.addAttribute("requestSurats", requestSurats);
        }
        
        if (requestSurats.size() == 0) {
            model.addAttribute("message", "Tidak ada data yang cocok dengan pencarian Anda.");
        }
                            
        return "user-history-diproses";
    }

    @GetMapping("/request/history/finished")
    @Transactional(readOnly = true)
    public String showFinishedRequests(Model model, Authentication auth) {
        List<RequestSurat> requestSurats = requestService.getAllFinishedRequestsSurat();
        model.addAttribute("requestSurats", requestSurats);

        List<SuratKeluar> suratKeluar = suratKeluarService.getAllSuratKeluar();
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

        return "user-history-selesai";
    }

    @GetMapping("/request/history/finished/search")
    @Transactional(readOnly = true)
    public String searchHistoryFinished(@RequestParam(name = "search", required = false) String search,
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

        List<RequestSurat> requestSurats; 
        if (search != null && !search.isEmpty()) {
            requestSurats = requestService.getBySearchAndStatusAndPengaju(5, search, pengaju);
            model.addAttribute("requestSurats", requestSurats);
        } else {
            // Jika tidak ada input, kembalikan semua permintaan surat
            requestSurats = requestService.getAllSubmittedRequestsSuratByPengaju(pengaju);
            model.addAttribute("requestSurats", requestSurats);
        }
        
        if (requestSurats.size() == 0) {
            model.addAttribute("message", "Tidak ada data yang cocok dengan pencarian Anda.");
        }
                            
        return "user-history-selesai";
    }

    // Admin
    @GetMapping("/admin/request/history")
    public String showAllRequest(Model model, Authentication auth) {
        List<RequestSurat> requestSurats = requestService.getAllRequestsSurat();
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

        return "riwayat-surat-admin";
    }

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

    // @GetMapping("/{requestSuratId}")
    // public ResponseEntity<RequestSurat> showDetailRequest(@PathVariable("requestSuratId") String requestSuratId) {
    //     try {
    //         RequestSurat requestSurat = requestService.getRequestSuratById(requestSuratId);
    //         return new ResponseEntity<>(requestSurat, HttpStatus.OK);
    //     } catch (NoSuchElementException e) {
    //         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RequestSurat with id: " + requestSuratId + " not found", e);
    //     }
    // }

    @GetMapping("/detail/{id}/submitted")
    @Transactional(readOnly = true)
    public String detailRequestSurat(@PathVariable("id") String id, Model model, Authentication auth) {
        RequestSurat requestSurats = requestService.getRequestSuratById(id);
        model.addAttribute("requestSurats", requestSurats);

        RequestTemplate file = requestService.getFile(id);

        if (file != null) {
            byte[] pdf = file.getFile();

            String base64PDF = Base64.getEncoder().encodeToString(pdf);

            model.addAttribute("base64PDF", base64PDF);
            model.addAttribute("template", file);
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

        return "user-detail-diajukan"; 
    }

    @GetMapping("/detail/{id}/rejected")
    @Transactional(readOnly = true)
    public String detailRequestSuratRejected(@PathVariable("id") String id, Model model, Authentication auth) {
        RequestSurat requestSurats = requestService.getRequestSuratById(id);
        model.addAttribute("requestSurats", requestSurats);
        
        RequestTemplate file = requestService.getFile(id);

        if (file != null) {
            byte[] pdf = file.getFile();

            String base64PDF = Base64.getEncoder().encodeToString(pdf);

            model.addAttribute("base64PDF", base64PDF);
            model.addAttribute("template", file);
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

        return "user-detail-ditolak"; 
    }

    @GetMapping("/detail/{id}/process")
    @Transactional(readOnly = true)
    public String detailRequestSuratOnProcess(@PathVariable("id") String id, Model model, Authentication auth) {
        RequestSurat requestSurats = requestService.getRequestSuratById(id);
        model.addAttribute("requestSurats", requestSurats);
        
        RequestTemplate file = requestService.getFile(id);

        if (file != null) {
            byte[] pdf = file.getFile();

            String base64PDF = Base64.getEncoder().encodeToString(pdf);

            model.addAttribute("base64PDF", base64PDF);
            model.addAttribute("template", file);
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

        return "user-detail-diproses"; 
    }

    @GetMapping("/detail/{id}/finished")
    @Transactional(readOnly = true)
    public String detailRequestSuratFinished(@PathVariable("id") String id, Model model, Authentication auth) {
        RequestSurat requestSurats = requestService.getRequestSuratById(id);
        model.addAttribute("requestSurats", requestSurats);

        SuratKeluar suratKeluar = suratKeluarService.getFileTtd(id);
        byte[] pdf = suratKeluar.getFile();

        SuratKeluar suratKeluar1 = requestSurats.getSurat();
        model.addAttribute("outgoing", suratKeluar1);

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

        return "user-detail-selesai"; 
    }

    @GetMapping("/detail/{id}/cancelled")
    @Transactional(readOnly = true)
    public String detailRequestSuratCancelled(@PathVariable("id") String id, Model model, Authentication auth) {
        RequestSurat requestSurats = requestService.getRequestSuratById(id);
        model.addAttribute("requestSurats", requestSurats);
        
        RequestTemplate file = requestService.getFile(id);

        if (file != null) {
            byte[] pdf = file.getFile();

            String base64PDF = Base64.getEncoder().encodeToString(pdf);

            model.addAttribute("base64PDF", base64PDF);
            model.addAttribute("template", file);
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

        return "user-detail-dibatalkan"; 
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

        RequestTemplate file = requestService.getFile(id);

        if (file != null) {
            byte[] pdf = file.getFile();

            String base64PDF = Base64.getEncoder().encodeToString(pdf);

            model.addAttribute("base64PDF", base64PDF);
            model.addAttribute("template", file);
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
        
        RequestTemplate file = requestService.getFile(id);

        if (file != null) {
            byte[] pdf = file.getFile();

            String base64PDF = Base64.getEncoder().encodeToString(pdf);

            model.addAttribute("base64PDF", base64PDF);
            model.addAttribute("template", file);
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

        return "admin-detail-dibatalkan"; 
    }

    @GetMapping("/admin/detail/{id}/rejected")
    @Transactional(readOnly = true)
    public String detailRequestSuratAdminTolak(@PathVariable("id") String id, Model model, Authentication auth) {
        RequestSurat requestSurats = requestService.getRequestSuratById(id);
        model.addAttribute("requestSurats", requestSurats);

        RequestTemplate file = requestService.getFile(id);

        if (file != null) {
            byte[] pdf = file.getFile();

            String base64PDF = Base64.getEncoder().encodeToString(pdf);

            model.addAttribute("base64PDF", base64PDF);
            model.addAttribute("template", file);
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

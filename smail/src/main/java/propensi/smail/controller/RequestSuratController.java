package propensi.smail.controller;

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
import propensi.smail.service.PenggunaService;
import propensi.smail.service.RequestService;
import propensi.smail.service.SuratKeluarService;
import propensi.smail.service.TemplateService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    @GetMapping("/request/history/search")
    public String searchHistory(@RequestParam(name = "searchValue", required = false) String searchValue,
                                Model model, Authentication auth) {

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

        if (searchValue != null && !searchValue.isEmpty()) {
            // Cek apakah searchValue merupakan format tanggal yang valid
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date tanggalPengajuan = dateFormat.parse(searchValue);
                List<RequestSurat> requestSurats = requestService.getRequestByTanggalPengajuan(tanggalPengajuan);
                model.addAttribute("requestSurats", requestSurats);
            } catch (ParseException e) {
                
                List<RequestSurat> requestSuratsByJenisSurat = requestService.getRequestByJenisSurat(searchValue);
                if (!requestSuratsByJenisSurat.isEmpty()) {
                    model.addAttribute("requestSurats", requestSuratsByJenisSurat);
                    return "user-history-diajukan";
                }

                List<RequestSurat> requestSuratsByBentukSurat = requestService.getRequestByBentukSurat(searchValue);
                if (!requestSuratsByBentukSurat.isEmpty()) {
                    model.addAttribute("requestSurats", requestSuratsByBentukSurat);
                    return "user-history-diajukan";
                }

                RequestSurat requestSuratsById = (RequestSurat) requestService.findRequestById(searchValue);
                if (requestSuratsById != null) {
                    model.addAttribute("requestSurats", requestSuratsById);
                    return "user-history-diajukan";
                }
                
                // Jika tidak ada yang cocok, kembalikan semua permintaan surat
                model.addAttribute("message", "Tidak ada data yang cocok dengan pencarian Anda.");
            }
        } else {
            // Jika tidak ada input, kembalikan semua permintaan surat
            List<RequestSurat> requestSurats = requestService.getAllRequestsSurat();
            model.addAttribute("requestSurats", requestSurats);
        }

        return "user-history-diajukan";
    }

    @GetMapping("/request/history/cancelled")
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
    public String searchHistoryCancelled(@RequestParam(name = "searchValue", required = false) String searchValue,
                                Model model, Authentication auth) {
        
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

        if (searchValue != null && !searchValue.isEmpty()) {
            // Cek apakah searchValue merupakan format tanggal yang valid
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date tanggalPengajuan = dateFormat.parse(searchValue);
                Date tanggalDibatalkan = dateFormat.parse(searchValue);
                List<RequestSurat> requestSurats = requestService.getRequestByTanggalPengajuanOrTanggalDibatalkan(tanggalPengajuan, tanggalDibatalkan);
                model.addAttribute("requestSurats", requestSurats);
            } catch (ParseException e) {
                
                List<RequestSurat> requestSuratsByJenisSurat = requestService.getRequestByJenisSurat(searchValue);
                if (!requestSuratsByJenisSurat.isEmpty()) {
                    model.addAttribute("requestSurats", requestSuratsByJenisSurat);
                    return "user-history-dibatalkan";
                }

                List<RequestSurat> requestSuratsByBentukSurat = requestService.getRequestByBentukSurat(searchValue);
                if (!requestSuratsByBentukSurat.isEmpty()) {
                    model.addAttribute("requestSurats", requestSuratsByBentukSurat);
                    return "user-history-dibatalkan";
                }

                RequestSurat requestSuratsById = (RequestSurat) requestService.findRequestById(searchValue);
                if (requestSuratsById != null) {
                    model.addAttribute("requestSurats", requestSuratsById);
                    return "user-history-dibatalkan";
                }
                
                // Jika tidak ada yang cocok, kembalikan semua permintaan surat
                model.addAttribute("message", "Tidak ada data yang cocok dengan pencarian Anda.");
            }
        } else {
            // Jika tidak ada input, kembalikan semua permintaan surat
            List<RequestSurat> requestSurats = requestService.getAllRequestsSurat();
            model.addAttribute("requestSurats", requestSurats);
        }

        return "user-history-dibatalkan";
    }

    @GetMapping("/request/history/rejected")
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
    public String searchHistoryRejected(@RequestParam(name = "searchValue", required = false) String searchValue,
                                Model model, Authentication auth) {

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

        if (searchValue != null && !searchValue.isEmpty()) {
            // Cek apakah searchValue merupakan format tanggal yang valid
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date tanggalPengajuan = dateFormat.parse(searchValue);
                Date tanggalPenolakan = dateFormat.parse(searchValue);
                List<RequestSurat> requestSurats = requestService.getRequestByTanggalPengajuanOrTanggalPenolakan(tanggalPengajuan, tanggalPenolakan);
                model.addAttribute("requestSurats", requestSurats);
            } catch (ParseException e) {
                
                List<RequestSurat> requestSuratsByJenisSurat = requestService.getRequestByJenisSurat(searchValue);
                if (!requestSuratsByJenisSurat.isEmpty()) {
                    model.addAttribute("requestSurats", requestSuratsByJenisSurat);
                    return "user-history-ditolak";
                }

                List<RequestSurat> requestSuratsByBentukSurat = requestService.getRequestByBentukSurat(searchValue);
                if (!requestSuratsByBentukSurat.isEmpty()) {
                    model.addAttribute("requestSurats", requestSuratsByBentukSurat);
                    return "user-history-ditolak";
                }

                RequestSurat requestSuratsById = (RequestSurat) requestService.findRequestById(searchValue);
                if (requestSuratsById != null) {
                    model.addAttribute("requestSurats", requestSuratsById);
                    return "user-history-ditolak";
                }
                
                // Jika tidak ada yang cocok, kembalikan semua permintaan surat
                model.addAttribute("message", "Tidak ada data yang cocok dengan pencarian Anda.");
            }
        } else {
            // Jika tidak ada input, kembalikan semua permintaan surat
            List<RequestSurat> requestSurats = requestService.getAllRequestsSurat();
            model.addAttribute("requestSurats", requestSurats);
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
    public String searchHistoryOnProcess(@RequestParam(name = "searchValue", required = false) String searchValue,
                                Model model, Authentication auth) {

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                String userId = pengguna.getId();
                List<RequestSurat> requestSurats = requestService.getAllFinishedRequestsSuratByPengaju(userId);
                model.addAttribute("requestSurats", requestSurats);
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        if (searchValue != null && !searchValue.isEmpty()) {
            // Cek apakah searchValue merupakan format tanggal yang valid
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date tanggalPengajuan = dateFormat.parse(searchValue);
                List<RequestSurat> requestSurats = requestService.getRequestByTanggalPengajuan(tanggalPengajuan);
                model.addAttribute("requestSurats", requestSurats);
            } catch (ParseException e) {
                
                List<RequestSurat> requestSuratsByJenisSurat = requestService.getRequestByJenisSurat(searchValue);
                if (!requestSuratsByJenisSurat.isEmpty()) {
                    model.addAttribute("requestSurats", requestSuratsByJenisSurat);
                    return "user-history-diproses";
                }

                List<RequestSurat> requestSuratsByBentukSurat = requestService.getRequestByBentukSurat(searchValue);
                if (!requestSuratsByBentukSurat.isEmpty()) {
                    model.addAttribute("requestSurats", requestSuratsByBentukSurat);
                    return "user-history-diproses";
                }

                RequestSurat requestSuratsById = (RequestSurat) requestService.findRequestById(searchValue);
                if (requestSuratsById != null) {
                    model.addAttribute("requestSurats", requestSuratsById);
                    return "user-history-diproses";
                }
                
                // Jika tidak ada yang cocok, kembalikan semua permintaan surat
                model.addAttribute("message", "Tidak ada data yang cocok dengan pencarian Anda.");
            }
        } else {
            // Jika tidak ada input, kembalikan semua permintaan surat
            List<RequestSurat> requestSurats = requestService.getAllRequestsSurat();
            model.addAttribute("requestSurats", requestSurats);
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
    public String searchHistoryFinished(@RequestParam(name = "searchValue", required = false) String searchValue,
                                Model model, Authentication auth) {

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

        if (searchValue != null && !searchValue.isEmpty()) {
            // Cek apakah searchValue merupakan format tanggal yang valid
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date tanggalPengajuan = dateFormat.parse(searchValue);
                Date tanggalSelesai = dateFormat.parse(searchValue);
                List<RequestSurat> requestSurats = requestService.getRequestByTanggalPengajuanOrTanggalSelesai(tanggalPengajuan, tanggalSelesai);
                model.addAttribute("requestSurats", requestSurats);
            } catch (ParseException e) {
                
                List<RequestSurat> requestSuratsByJenisSurat = requestService.getRequestByJenisSurat(searchValue);
                if (!requestSuratsByJenisSurat.isEmpty()) {
                    model.addAttribute("requestSurats", requestSuratsByJenisSurat);
                    return "user-history-selesai";
                }

                SuratKeluar requestSuratsByNomorSurat = suratKeluarService.findSuratKeluarByNomorArsip(searchValue);
                if (requestSuratsByNomorSurat != null) {
                    model.addAttribute("requestSurats", requestSuratsByNomorSurat);
                    return "user-history-selesai";
                }

                RequestSurat requestSuratsById = (RequestSurat) requestService.findRequestById(searchValue);
                if (requestSuratsById != null) {
                    model.addAttribute("requestSurats", requestSuratsById);
                    return "user-history-selesai";
                }
                
                // Jika tidak ada yang cocok, kembalikan semua permintaan surat
                model.addAttribute("message", "Tidak ada data yang cocok dengan pencarian Anda.");
            }
        } else {
            // Jika tidak ada input, kembalikan semua permintaan surat
            List<RequestSurat> requestSurats = requestService.getAllRequestsSurat();
            model.addAttribute("requestSurats", requestSurats);
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

    @GetMapping("/{requestSuratId}")
    public ResponseEntity<RequestSurat> showDetailRequest(@PathVariable("requestSuratId") String requestSuratId) {
        try {
            RequestSurat requestSurat = requestService.getRequestSuratById(requestSuratId);
            return new ResponseEntity<>(requestSurat, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RequestSurat with id: " + requestSuratId + " not found", e);
        }
    }

    @GetMapping("/detail/{id}/submitted")
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

    @GetMapping("/detail/{id}/rejected")
    public String detailRequestSuratRejected(@PathVariable("id") String id, Model model, Authentication auth) {
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

        return "user-detail-ditolak"; 
    }

    @GetMapping("/detail/{id}/process")
    public String detailRequestSuratOnProcess(@PathVariable("id") String id, Model model, Authentication auth) {
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

        return "user-detail-diproses"; 
    }

    @GetMapping("/detail/{id}/finished")
    public String detailRequestSuratFinished(@PathVariable("id") String id, Model model, Authentication auth) {
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

        return "user-detail-selesai"; 
    }

    @GetMapping("/detail/{id}/cancelled")
    public String detailRequestSuratCancelled(@PathVariable("id") String id, Model model, Authentication auth) {
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

        return "user-detail-dibatalkan"; 
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
    @Transactional(readOnly = true)
    public String showAllProcessingRequestsAdmin(Model model, Authentication auth) {
        try {
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
        } catch (Exception e) {
            // Handle exception
            return "error-page";
        }
    }

    @GetMapping("/admin/request/finished")
    @Transactional(readOnly = true)
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
    public String updateStatus(@PathVariable("id") String id, @RequestParam("status") int status, @RequestParam(value = "alasanPenolakan", required = false) String alasanPenolakan,
                               Model model, Authentication auth) {
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
        } else {
            requestSurat.setAlasanPenolakan(null);
        }

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

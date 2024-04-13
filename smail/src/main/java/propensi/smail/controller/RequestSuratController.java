package propensi.smail.controller;

import org.springframework.web.bind.annotation.*;

import propensi.smail.model.user.*;
import propensi.smail.dto.RequestAndFieldDataDTO;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.TemplateSurat;
import propensi.smail.repository.PenggunaDb;
import propensi.smail.model.RequestTemplate;
import propensi.smail.service.PenggunaService;
import propensi.smail.service.RequestService;
import propensi.smail.service.TemplateService;

import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ArrayList;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.validation.ObjectError;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Controller
// @RestController
// @RequestMapping("/request")
public class RequestSuratController {
    
    @Autowired
    private RequestService requestService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    PenggunaDb penggunaDb;

    @Autowired
    PenggunaService penggunaService;

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
            List<String> bentukSurat = requestDTO.getBentukSurat();
            requestSurat.setBentukSurat(bentukSurat);
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

        return "riwayat-surat";
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

    @PutMapping("/{requestSuratId}/cancel")
    public ResponseEntity<RequestSurat> cancelRequest(@PathVariable("requestSuratId") String requestSuratId) {
        try {
            RequestSurat canceledRequestSurat = requestService.batalkanRequestSurat(requestSuratId);
            return new ResponseEntity<>(canceledRequestSurat, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RequestSurat with id: " + requestSuratId + " not found", e);
        }
    }
}

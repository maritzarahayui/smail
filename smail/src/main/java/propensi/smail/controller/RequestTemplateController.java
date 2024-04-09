package propensi.smail.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import propensi.smail.model.RequestTemplate;
import propensi.smail.repository.PenggunaDb;
import propensi.smail.repository.RequestTemplateDb;
import propensi.smail.service.PenggunaService;
import propensi.smail.service.RequestService;

import java.util.Map;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Controller
//@RestController
//@RequestMapping("/api/request-template")
public class RequestTemplateController {
    @Autowired
    private RequestTemplateDb requestTemplateDb;

    @Autowired
    private RequestService requestService;

    @Autowired
    PenggunaDb penggunaDb;

    @Autowired
    PenggunaService penggunaService;

//    @PostMapping(value = "/new")
//    private RequestTemplate restRequestTemplate(@Valid @RequestBody RequestTemplate requestTemplate, BindingResult bindingResult){
//        if(bindingResult.hasFieldErrors()){
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST, "Request body has invalid type or missing field"
//            );
//        } else {
//            requestService.createRequestTemplate(requestTemplate);
//            return requestTemplate;
//        }
//    }

    @GetMapping(value = "/allTemplate")
    private List<RequestTemplate> retrieveAllRequest(){
        return requestService.retrieveAllRequest();
    }

    // public String formRequestTemplate(Model model){
    //     Map<Integer, String> listKategori = requestService.listKategori();
    //     model.addAttribute("listKategori", listKategori);

    //     Map<Integer, String> listBahasa = requestService.listBahasa();
    //     model.addAttribute("listBahasa", listBahasa);

    //     model.addAttribute("requestTemplate", new RequestTemplate());
    //     return "request-surat";
    // }

    // // @PostMapping("request-template")
    // public String requestTemplate(@Valid @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ModelAttribute RequestTemplate requestTemplate, 
    //                                 BindingResult bindingResult) {
    //     if (bindingResult.hasErrors()) {
    //         return bindingResult.getAllErrors().toString(); // Ini hanya contoh, Anda bisa menyesuaikan format pesan error
    //     }
    //     try {
    //         requestService.createRequestTemplate(requestTemplate);
    //         System.out.println("BERHASIL");
    //         return "redirect:/request-surat"; // Redirect ke halaman history jika berhasil
    //     } catch (Exception e) {
    //         // Tangkap dan tangani kesalahan jika terjadi
    //         e.printStackTrace();
    //         return "Gagal membuat permintaan surat: " + e.getMessage(); 
    //     }
    // }
}

package propensi.smail.controller;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import propensi.smail.model.user.Pengguna;
import propensi.smail.repository.PenggunaDb;
import propensi.smail.service.FAQService;
import propensi.smail.service.PenggunaService;
import propensi.smail.service.RequestService;
import propensi.smail.service.SuratKeluarService;
import propensi.smail.service.SuratMasukService;
import propensi.smail.service.TemplateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;


@Controller
public class BaseController {
    
    @Autowired
    PenggunaDb penggunaDb;
    // hapus
    @Autowired
    private SuratMasukService suratMasukService;

    @Autowired
    PenggunaService penggunaService;

    @Autowired
    SuratKeluarService suratKeluarService;

    @Autowired
    RequestService requestService;

    @Autowired
    FAQService faqService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private TemplateService templateService;

    @GetMapping("/")
    public String home(Model model, Authentication auth) {

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                String role = penggunaService.getRole(pengguna);
                String role = penggunaService.getRole(pengguna);

                model.addAttribute("role", role);
                model.addAttribute("role", role);
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));

                if (role.equals("Admin")) {
                    /* model.addAttribute yg dibutuhin */
                    return "dashboard-pengurus";
                } else if (role.equals("Pengurus")) {
                    /* model.addAttribute yg dibutuhin */
                    model.addAttribute("mapSuratMasukTahun", suratMasukService.getJumlahSuratMasukTahunIni());
                    model.addAttribute("mapSuratMasukBulan", suratMasukService.getJumlahSuratMasukBulanIni());
                    model.addAttribute("mapSuratMasukMinggu", suratMasukService.getJumlahSuratMasukMingguIni());

                    model.addAttribute("mapSuratKeluarTahun", suratKeluarService.getJumlahSuratKeluarTahunIni());
                    model.addAttribute("mapSuratKeluarBulan", suratKeluarService.getJumlahSuratKeluarBulanIni());
                    model.addAttribute("mapSuratKeluarMinggu", suratKeluarService.getJumlahSuratKeluarMingguIni());

                    model.addAttribute("performaRequestSurat", requestService.getPerformaRequestSurat());

                    model.addAttribute("mapSuratMasukKategori", suratMasukService.getJumlahSuratMasukPerKategori());
                    model.addAttribute("mapSuratKeluarKategori", suratKeluarService.getJumlahSuratKeluarPerKategori());

                    return "dashboard-pengurus";
                } else if (role.equals("Dosen")) {
                    /* model.addAttribute yg dibutuhin */
                    return "dashboard-pengurus";
                } else {
                    /* model.addAttribute yg dibutuhin */
                    return "dashboard-staf-mhs";
                } 

                if (role.equals("Admin")) {
                    Map<String, Long> bulan = requestService.getJumlahRequestPerMonth();
                    // System.out.println(bulan);
                    model.addAttribute("bulan", bulan.keySet().iterator().next());

                    Map<String, Long> jumlahRequestPerMinggu = requestService.getJumlahRequestPerMinggu();
                    // System.out.println(jumlahRequestPerMinggu);
                    model.addAttribute("jumlahRequestPerMinggu", jumlahRequestPerMinggu);

                    int submittedRequest = requestService.getAllSubmitedRequestsSurat().size();
                    model.addAttribute("diajukan", submittedRequest);

                    int rejectedRequest = requestService.getAllRejectedRequestsSurat().size();
                    model.addAttribute("ditolak", rejectedRequest);

                    int canceledRequest = requestService.getAllCanceledRequestsSurat().size();
                    model.addAttribute("dibatalkan", canceledRequest);

                    int onProcessRequest = requestService.getAllOnProcessRequestsSurat().size();
                    model.addAttribute("diproses", onProcessRequest);

                    int finishedRequest = requestService.getAllFinishedRequestsSurat().size();
                    model.addAttribute("selesai", finishedRequest);

                    long reqTemplateDiterima = templateService.getAllReqTemplate().stream()
                            .filter(template -> template.getStatus() == 2)
                            .count();

                    long reqTemplateDitolak = templateService.getAllReqTemplate().stream()
                            .filter(template -> template.getStatus() == 3)
                            .count();

                    model.addAttribute("reqTemplateDiterima", reqTemplateDiterima);
                    model.addAttribute("reqTemplateDitolak", reqTemplateDitolak);

                    Map<String, Long> jumlahRequestByKategori = requestService.getJumlahRequestByKategori();
                    // System.out.println(jumlahRequestByKategori);
                    model.addAttribute("jumlahRequestByKategori", jumlahRequestByKategori);

                    Map<String, Long> activeTemplateByKategori = templateService.getActiveTemplateByKategori();
                    // System.out.println(activeTemplateByKategori);
                    model.addAttribute("activeTemplateByKategori", activeTemplateByKategori);

                    int notAnsweredFaq = faqService.getAllNotAnsweredFaq().size();
                    model.addAttribute("notAnsweredFaq", notAnsweredFaq);

                    int dieskalasiFaq = faqService.getAllEskalasiFaq().size();
                    model.addAttribute("dieskalasiFaq", dieskalasiFaq);

                    int answeredFaq = faqService.getAllAnsweredFaq().size();
                    model.addAttribute("answeredFaq", answeredFaq);

                    int deletedFaq = faqService.getAllDeletedFaq().size();
                    model.addAttribute("deletedFaq", deletedFaq);

                    List<String> allRoles = penggunaService.getAllRoles();
                    model.addAttribute("allRoles", allRoles);

                    Map<String, Long> jumlahRequestByRole = requestService.getJumlahRequestByRole();
                    // System.out.println(jumlahRequestByRole);
                    model.addAttribute("jumlahRequestByRole", jumlahRequestByRole);

                    String topRequester = requestService.getTopRequester();
                    model.addAttribute("topRequester", topRequester);

                    return "dashboard-admin";
                } else if (role.equals("Pengurus")) {
                    return "dashboard-pengurus";
                } else if (role.equals("Dosen")) {
                    /* model.addAttribute yg dibutuhin */
                    return "dashboard-admin";
                } else {
                    /* model.addAttribute yg dibutuhin */
                    return "dashboard-staf-mhs";
                }
            } else {
                return "auth-failed";
            }
        }
        return "login";
    }

    // @GetMapping("/")
    // public String home(Model model, Authentication auth) {

    //     if (auth != null) {
    //         OidcUser oauthUser = (OidcUser) auth.getPrincipal();
    //         String email = oauthUser.getEmail();
    //         Optional<Pengguna> user = penggunaDb.findByEmail(email);

    //         if (user.isPresent()) {
    //             Pengguna pengguna = user.get();
    //             model.addAttribute("nama", pengguna.getNama());
    //             model.addAttribute("email", email);
    //             model.addAttribute("email_pengguna", pengguna.getEmail());
    //             model.addAttribute("id", pengguna.getId());

    //             model.addAttribute("role", penggunaService.getRole(pengguna));
    //             model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));

    //             model.addAttribute("faqsTerjawab", faqService.getFaqsByStatus(2));

    //             return "home";
    //         } else {
    //             return "auth-failed";
    //         }

    //     }
    //     // asli:
    //     return "login";
    // }

    @GetMapping("/profile")
    public String profile(Model model, Authentication auth) {

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                model.addAttribute("nama", pengguna.getNama());
                model.addAttribute("id", pengguna.getId());
                model.addAttribute("email", pengguna.getEmail());

                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));

            } else {
                return "auth-failed";
            }

        }

        return "profile";
    }

    @GetMapping("/login")
    public String login(Authentication auth) {

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();

            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                return "login";
            } else {
                return "auth-failed";
            }

        } return "login";
    }

    @GetMapping("/invalid-auth")
    public String failed() {
        return "auth-failed";
    }

    
}
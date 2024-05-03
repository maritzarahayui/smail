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

    @Autowired
    SuratMasukService suratMasukService;

    @Autowired
    PenggunaService penggunaService;

    @Autowired
    SuratKeluarService suratKeluarService;

    @Autowired
    RequestService requestService;

    @Autowired
    FAQService faqService;

    @Autowired
    TemplateService templateService;

    @GetMapping("/")
    public String home(Model model, Authentication auth) {

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                String role = penggunaService.getRole(pengguna);

                model.addAttribute("role", role);
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));

                if (role.equals("Admin")) {
                    Map<String, Long> bulan = requestService.getJumlahRequestPerMonth();
                    if (!bulan.isEmpty()) {
                        model.addAttribute("bulan", bulan.keySet().iterator().next());
                        model.addAttribute("jumlahRequestPerBulan", bulan);
                    } else {
                        model.addAttribute("bulan", "");
                    }

                    Map<String, Long> tahun = requestService.getJumlahRequestPerYear();
                    if (!tahun.isEmpty()) {
                        model.addAttribute("tahun", tahun.keySet().iterator().next());
                    } else {
                        model.addAttribute("tahun", "");
                    }

                    model.addAttribute("jumlahRequestPerMinggu", requestService.getJumlahRequestPerMinggu());
                    model.addAttribute("performaRequestSurat", requestService.getPerformaRequestSurat());
                    model.addAttribute("diajukan", requestService.getAllSubmitedRequestsSurat().size());
                    model.addAttribute("ditolak", requestService.getAllRejectedRequestsSurat().size());
                    model.addAttribute("dibatalkan", requestService.getAllCanceledRequestsSurat().size());
                    model.addAttribute("diproses", requestService.getAllOnProcessRequestsSurat().size());
                    model.addAttribute("selesai", requestService.getAllFinishedRequestsSurat().size());
                    model.addAttribute("jumlahRequestByKategori", requestService.getJumlahRequestByKategori());
                    model.addAttribute("jumlahRequestByRole", requestService.getJumlahRequestByRole());
                    model.addAttribute("topRequester", requestService.getTopRequester());

                    model.addAttribute("reqTemplateDiterima", templateService.getAllReqTemplate().stream()
                            .filter(template -> template.getStatus() == 2).count());
                    model.addAttribute("reqTemplateDitolak", templateService.getAllReqTemplate().stream()
                            .filter(template -> template.getStatus() == 3).count());
                    model.addAttribute("activeTemplateByKategori", templateService.getActiveTemplateByKategori());

                    model.addAttribute("notAnsweredFaq", faqService.getAllNotAnsweredFaq().size());
                    model.addAttribute("dieskalasiFaq", faqService.getAllEskalasiFaq().size());
                    model.addAttribute("answeredFaq", faqService.getAllAnsweredFaq().size());
                    model.addAttribute("deletedFaq", faqService.getAllDeletedFaq().size());

                    model.addAttribute("allRoles", penggunaService.getAllRoles());

                    model.addAttribute("mapSuratMasukKategori", suratMasukService.getJumlahSuratMasukPerKategori());
                    model.addAttribute("mapSuratMasukMinggu", suratMasukService.getJumlahSuratMasukMingguIni());

                    model.addAttribute("mapSuratKeluarMinggu", suratKeluarService.getJumlahSuratKeluarMingguIni());

                    return "dashboard-admin";
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
package propensi.smail.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import propensi.smail.model.*;
import propensi.smail.model.user.*;
import propensi.smail.repository.*;
import propensi.smail.service.*;

@Controller
@RequestMapping("/faq")
public class FAQController {

    @Autowired
    private FAQService faqService;

    @Autowired
    private PenggunaService penggunaService;

    @Autowired
    private PenggunaDb penggunaDb;

    @GetMapping
    public String showFAQ(Model model, Authentication auth, @RequestParam(required = false) String activeTab) {

        List<FAQ> faqsBelumDijawab = faqService.getFaqsByStatus(0);
        List<FAQ> faqsDieskalasi = faqService.getFaqsByStatus(1);
        List<FAQ> faqsTerjawab = faqService.getFaqsByStatus(2);
        List<FAQ> faqsDihapus = faqService.getFaqsByStatus(3);

        model.addAttribute("faqsBelumDijawab", faqsBelumDijawab);
        model.addAttribute("faqsDieskalasi", faqsDieskalasi);
        model.addAttribute("faqsTerjawab", faqsTerjawab);
        model.addAttribute("faqsDihapus", faqsDihapus);

        OidcUser oauthUser = (OidcUser) auth.getPrincipal();
        String email = oauthUser.getEmail();
        Optional<Pengguna> user = penggunaDb.findByEmail(email);

        if (user.isPresent()) {
            Pengguna pengguna = user.get();
            String role = penggunaService.getRole(pengguna);
            model.addAttribute("role", role);
            model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));

            if (role.equals("Admin")) {
                model.addAttribute("activeTab", activeTab != null ? activeTab : "#belumDijawab");
                return "faq-all-admin";
            } else if (role.equals("Pengurus")) {
                model.addAttribute("activeTab", activeTab != null ? activeTab : "#dieskalasi");
                return "faq-all-pengurus";
            } else {
                model.addAttribute("newFaq", new FAQ());
                return "faq-all-user";
            }

        } else {
            return "auth-failed";
        }

    }
    
    @PostMapping("/tanya")
    public String tanyaFAQ(Model model, Authentication auth, FAQ faqDTO) {
        
        Pengguna pengguna = null;
        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                pengguna = user.get();
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        faqService.createFAQ(faqDTO, pengguna);
        return "redirect:/faq";
    }


    @GetMapping("/{idFAQ}/jawab")
    public String formJawabFAQ(Model model, Authentication auth, @PathVariable("idFAQ") int idFAQ) {
        FAQ faq = faqService.getFAQbyId(idFAQ);

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

        model.addAttribute("newFaq", faq);
        return "faq-jawab";
    }

    @PostMapping("/{idFAQ}/jawab")
    public String jawabFAQ(Model model, Authentication auth, @PathVariable("idFAQ") int idFAQ, FAQ newFaq) {
        
        FAQ faq = faqService.jawabFAQ(newFaq);

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

        return "redirect:/faq";
    }

    @GetMapping("/{idFAQ}/detail")
    public String detailFAQ(Model model, Authentication auth, @PathVariable("idFAQ") int idFAQ) {
        FAQ faq = faqService.getFAQbyId(idFAQ);
        model.addAttribute("faq", faq);

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

        return "faq-detail";
    }

    @GetMapping("/{idFAQ}/edit")
    public String formEditFAQ(Model model, Authentication auth, @PathVariable("idFAQ") int idFAQ) {
        FAQ faq = faqService.getFAQbyId(idFAQ);

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

        model.addAttribute("newFaq", faq);
        return "faq-edit";
    }

    @PostMapping("/{idFAQ}/edit")
    public String editFAQ(Model model, Authentication auth, @PathVariable("idFAQ") int idFAQ, FAQ newFaq) {
    
        FAQ faq = faqService.editFAQ(newFaq);

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

        return "redirect:/faq";
    }

    @GetMapping("/{idFAQ}/eskalasi")
    public String eskalasiFAQ(Model model, Authentication auth, @PathVariable("idFAQ") int idFAQ) {
        FAQ faq = faqService.eskalasiFAQ(idFAQ);
        return "redirect:/faq";
    }

    @GetMapping("/{idFAQ}/hapus")
    public String deleteFAQ(Model model, Authentication auth, @PathVariable("idFAQ") int idFAQ) {
        FAQ faq = faqService.deleteFAQ(idFAQ);
        return "redirect:/faq";
    }

    @GetMapping("/user-faq/terjawab")
    public String userFAQTerjawab(Model model, Authentication auth) {
       
        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                String role = penggunaService.getRole(pengguna);

                List<FAQ> faqsBelumDijawab = faqService.getFaqsByPengajuAndStatus(pengguna, 0);
                List<FAQ> faqsTerjawab = faqService.getFaqsByPengajuAndStatus(pengguna, 2);

                model.addAttribute("role", role);
                model.addAttribute("faqsUserTerjawab", faqsTerjawab);
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));

                return "user-faq-terjawab";
            } else {
                return "auth-failed";
            }   
        }
        return "user-faq-terjawab";
    }

    @GetMapping("/user-faq/belum-terjawab")
    public String userFAQBelumTerjawab(Model model, Authentication auth) {

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                String role = penggunaService.getRole(pengguna);
                List<FAQ> faqsBelumDijawab = faqService.getFaqsByPengajuAndStatus(pengguna, 0);
                List<FAQ> faqsTerjawab = faqService.getFaqsByPengajuAndStatus(pengguna, 2);

                model.addAttribute("role", role);
                model.addAttribute("faqsUserBelumTerjawab", faqsBelumDijawab);
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));

                return "user-faq-belum-terjawab";
            } else {
                return "auth-failed";
            }
            
        }
        return "user-faq-belum-terjawab";
    }

}

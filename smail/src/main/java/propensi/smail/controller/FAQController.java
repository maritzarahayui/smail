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
import propensi.smail.repository.PenggunaDb;
import propensi.smail.service.FAQService;
import propensi.smail.service.PenggunaService;

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
    public String showFAQ(Model model, Authentication auth, @RequestParam(name = "search", required = false) String search) {

        List<FAQ> faqsBelumDijawab;
        List<FAQ> faqsDieskalasi;
        List<FAQ> faqsTerjawab;
        List<FAQ> faqsDihapus;

        if (search != null && !search.isEmpty()) {
            faqsBelumDijawab = faqService.getFaqsByStatusAndSearch(search,0);
            faqsDieskalasi = faqService.getFaqsByStatusAndSearch(search, 1);
            faqsTerjawab = faqService.getFaqsByStatusAndSearch(search, 2);
            faqsDihapus = faqService.getFaqsByStatusAndSearch(search, 3);
        } else {
            faqsBelumDijawab = faqService.getFaqsByStatus(0);
            faqsDieskalasi = faqService.getFaqsByStatus(1);
            faqsTerjawab = faqService.getFaqsByStatus(2);
            faqsDihapus = faqService.getFaqsByStatus(3);
        }

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
                return "faq-all-admin";
            } else if (role.equals("Pengurus")) {
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

}

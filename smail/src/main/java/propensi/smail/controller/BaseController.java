package propensi.smail.controller;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import propensi.smail.model.FAQ;
import propensi.smail.model.user.Pengguna;
import propensi.smail.repository.PenggunaDb;
import propensi.smail.service.FAQService;
import propensi.smail.service.PenggunaService;
import propensi.smail.service.RequestService;
import propensi.smail.service.SuratMasukService;

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
    FAQService faqService;

    @Autowired
    RequestService requestService;

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
                    return "home";
                } else if (role.equals("Pengurus")) {
                    return "dashboard-pengurus";
                    
                } else if (role.equals("Dosen")) {
                    // Jumlah request surat (bulan, tahun)
                    Map<String, Long> bulan = requestService.getJumlahRequestPerMonthByUser(pengguna);
                    if (bulan.isEmpty()) {
                        model.addAttribute("bulan", "");
                    } else {
                        model.addAttribute("bulan", bulan.keySet().iterator().next());
                    }
                    model.addAttribute("jumlahRequestPerBulan", bulan);

                    Map<String, Long> tahun = requestService.getJumlahRequestPerYearByUser(pengguna);
                    if (tahun.isEmpty()) {
                        model.addAttribute("tahun", "");
                    } else {
                        model.addAttribute("tahun", tahun.keySet().iterator().next());
                    }
                    model.addAttribute("jumlahRequestPerTahun", tahun);

                    Map<String, Long> minggu = requestService.getJumlahRequestPerMingguByUser(pengguna);
                    if (minggu.isEmpty()) {
                        model.addAttribute("minggu", "");
                    } else {
                        model.addAttribute("minggu", minggu.keySet().iterator().next());
                    }
                    model.addAttribute("jumlahRequestPerMinggu", minggu);

                    // Statistik jumlah req tiap status (OK)
                    Map<String, Long> jumlahRequestByStatus = requestService.getJumlahRequestByStatus(pengguna);
                    model.addAttribute("diajukan", jumlahRequestByStatus.getOrDefault("Diajukan", 0L));
                    model.addAttribute("dibatalkan", jumlahRequestByStatus.getOrDefault("Dibatalkan", 0L));
                    model.addAttribute("ditolak", jumlahRequestByStatus.getOrDefault("Ditolak", 0L));
                    model.addAttribute("diproses", jumlahRequestByStatus.getOrDefault("Diproses", 0L));
                    model.addAttribute("selesai", jumlahRequestByStatus.getOrDefault("Selesai", 0L)); 

                    // Statistik jumlah pertanyaan terjawab
                    Map<String, Long> statistikTerjawab = faqService.getCountOfAnsweredQuestions(pengguna);
                    model.addAttribute("answeredFAQs", statistikTerjawab.getOrDefault("Terjawab", 0L));

                    // Statistik jumlah req by kategori
                    Map<String, Long> requestByCategory = requestService.getCountOfRequestByCategory(pengguna);
                    model.addAttribute("requestByCategory", requestByCategory);
                
                    return "dashboard-dosen";
                } else if (role.equals("Staf")) {
                    // Jumlah request surat (bulan, tahun)
                    Map<String, Long> bulan = requestService.getJumlahRequestPerMonthByUser(pengguna);
                    System.out.println(bulan);
                    if (bulan.isEmpty()) {
                        model.addAttribute("bulan", "");
                    } else {
                        model.addAttribute("bulan", bulan.keySet().iterator().next());
                    }
                    model.addAttribute("jumlahRequestPerBulan", bulan);

                    Map<String, Long> tahun = requestService.getJumlahRequestPerYearByUser(pengguna);
                    if (tahun.isEmpty()) {
                        model.addAttribute("tahun", "");
                    } else {
                        model.addAttribute("tahun", tahun.keySet().iterator().next());
                    }
                    model.addAttribute("jumlahRequestPerTahun", tahun);

                    Map<String, Long> minggu = requestService.getJumlahRequestPerMingguByUser(pengguna);
                    if (minggu.isEmpty()) {
                        model.addAttribute("minggu", "");
                    } else {
                        model.addAttribute("minggu", minggu.keySet().iterator().next());
                    }
                    model.addAttribute("jumlahRequestPerMinggu", minggu);

                    // Statistik jumlah req tiap status
                    Map<String, Long> jumlahRequestByStatus = requestService.getJumlahRequestByStatus(pengguna);
                    model.addAttribute("diajukan", jumlahRequestByStatus.getOrDefault("Diajukan", 0L));
                    model.addAttribute("dibatalkan", jumlahRequestByStatus.getOrDefault("Dibatalkan", 0L));
                    model.addAttribute("ditolak", jumlahRequestByStatus.getOrDefault("Ditolak", 0L));
                    model.addAttribute("diproses", jumlahRequestByStatus.getOrDefault("Diproses", 0L));
                    model.addAttribute("selesai", jumlahRequestByStatus.getOrDefault("Selesai", 0L)); 

                    // Statistik jumlah pertanyaan terjawab
                    Map<String, Long> statistikTerjawabStaf = faqService.getCountOfAnsweredQuestions(pengguna);
                    model.addAttribute("answeredFAQs", statistikTerjawabStaf.getOrDefault("Terjawab", 0L));

                    // Statistik jumlah req by kategori
                    Map<String, Long> requestByCategory = requestService.getCountOfRequestByCategory(pengguna);
                    model.addAttribute("requestByCategory", requestByCategory);

                    return "dashboard-staf";
                } else if (role.equals("Mahasiswa")){
                    // Jumlah request surat (bulan, tahun)
                    Map<String, Long> bulan = requestService.getJumlahRequestPerMonthByUser(pengguna);
                    System.out.println(bulan);
                    if (bulan.isEmpty()) {
                        model.addAttribute("bulan", "");
                    } else {
                        model.addAttribute("bulan", bulan.keySet().iterator().next());
                    }
                    model.addAttribute("jumlahRequestPerBulan", bulan);

                    Map<String, Long> tahun = requestService.getJumlahRequestPerYearByUser(pengguna);
                    if (tahun.isEmpty()) {
                        model.addAttribute("tahun", "");
                    } else {
                        model.addAttribute("tahun", tahun.keySet().iterator().next());
                    }
                    model.addAttribute("jumlahRequestPerTahun", tahun);

                    Map<String, Long> minggu = requestService.getJumlahRequestPerMingguByUser(pengguna);
                    if (minggu.isEmpty()) {
                        model.addAttribute("minggu", "");
                    } else {
                        model.addAttribute("minggu", minggu.keySet().iterator().next());
                    }
                    model.addAttribute("jumlahRequestPerMinggu", minggu);

                    // Statistik jumlah req tiap status
                     Map<String, Long> jumlahRequestByStatus = requestService.getJumlahRequestByStatus(pengguna);
                    model.addAttribute("diajukan", jumlahRequestByStatus.getOrDefault("Diajukan", 0L));
                    model.addAttribute("dibatalkan", jumlahRequestByStatus.getOrDefault("Dibatalkan", 0L));
                    model.addAttribute("ditolak", jumlahRequestByStatus.getOrDefault("Ditolak", 0L));
                    model.addAttribute("diproses", jumlahRequestByStatus.getOrDefault("Diproses", 0L));
                    model.addAttribute("selesai", jumlahRequestByStatus.getOrDefault("Selesai", 0L)); 

                    // Statistik jumlah pertanyaan terjawab
                    Map<String, Long> statistikTerjawabMahasiswa = faqService.getCountOfAnsweredQuestions(pengguna);
                    model.addAttribute("answeredFAQs", statistikTerjawabMahasiswa.getOrDefault("Terjawab", 0L));

                    // Statistik jumlah req by jenis
                    Map<String, Long> requestByJenis = requestService.getCountOfRequestByJenis(pengguna);
                    model.addAttribute("requestByJenis", requestByJenis);

                    return "dashboard-mhs";
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
                return "home";
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
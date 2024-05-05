package propensi.smail.controller;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
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
                    Map<String, Map<String, Long>> dataJumlahRequest = requestService.getJumlahRequestPerYearAndMonth();
                    if (!dataJumlahRequest.isEmpty()) {
                        String tahun = dataJumlahRequest.keySet().iterator().next();
                        Map<String, Long> bulanDanJumlahRequest = dataJumlahRequest.get(tahun);

                        if (!bulanDanJumlahRequest.isEmpty()) {
                            String bulan = bulanDanJumlahRequest.keySet().iterator().next();

                            model.addAttribute("bulan", bulan);
                            model.addAttribute("tahun", tahun);
                            model.addAttribute("jumlahRequestPerBulan", bulanDanJumlahRequest);
                        } else {
                            model.addAttribute("bulan", "");
                            model.addAttribute("tahun", tahun);
                            model.addAttribute("jumlahRequestPerBulan", Collections.emptyMap());
                        }
                    } else {
                        model.addAttribute("bulan", "");
                        model.addAttribute("tahun", "");
                        model.addAttribute("jumlahRequestPerBulan", Collections.emptyMap());
                    }

                    model.addAttribute("jumlahRequestPerMinggu", requestService.getJumlahRequestPerMinggu());
                    model.addAttribute("currentYearMonth", requestService.getCurrentYearMonth());

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
                    model.addAttribute("mapSuratKeluarKategori", suratKeluarService.getJumlahSuratKeluarPerKategori());
                   
                    model.addAttribute("mapSuratMasukTahun", suratMasukService.getJumlahSuratMasukTahunIni());
                    model.addAttribute("mapSuratMasukBulan", suratMasukService.getJumlahSuratMasukBulanIni());
                    model.addAttribute("mapSuratMasukMinggu", suratMasukService.getJumlahSuratMasukMingguIni());

                    model.addAttribute("mapSuratKeluarTahun", suratKeluarService.getJumlahSuratKeluarTahunIni());
                    model.addAttribute("mapSuratKeluarBulan", suratKeluarService.getJumlahSuratKeluarBulanIni());
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

                    var mapSuratTtd = suratKeluarService.getJumlahSuratKeluarTandaTangan(pengguna);
                    model.addAttribute("mapSuratKeluarTtd", mapSuratTtd);

                    var mapSuratMasukStatus = suratMasukService.getJumlahSuratMasukPerStatus();
                    model.addAttribute("mapSuratMasukStatus", mapSuratMasukStatus);

                    model.addAttribute("totalSuratKeluar", suratKeluarService.getAllSuratKeluar().size());
                    model.addAttribute("totalSuratMasuk", suratMasukService.getAllSuratMasuk().size());
                    model.addAttribute("totalForumEsk", faqService.getFaqsByStatus(1).size());
                    model.addAttribute("totalTtd", mapSuratTtd.get("Sudah")+mapSuratTtd.get("Belum"));

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

                    var mapSuratTtd = suratKeluarService.getJumlahSuratKeluarTandaTangan(pengguna);
                    model.addAttribute("totalTtd", mapSuratTtd.get("Sudah")+mapSuratTtd.get("Belum"));
                
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

        List<FAQ> faqsTerjawab = faqService.getFaqsByStatus(2);
        model.addAttribute("faqsTerjawab", faqsTerjawab);

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
    public String login(Authentication auth, Model model) {

        List<FAQ> faqsTerjawab = faqService.getFaqsByStatus(2);
        model.addAttribute("faqsTerjawab", faqsTerjawab);

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
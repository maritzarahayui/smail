package propensi.smail.controller;

import org.springframework.web.bind.annotation.*;

import propensi.smail.model.Pengguna;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.TemplateSurat;
import propensi.smail.model.RequestTemplate;
import propensi.smail.model.Role;
import propensi.smail.service.RequestService;
import propensi.smail.service.TemplateService;

import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;
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


@Controller
// @RestController
// @RequestMapping("/api/request-surat")
public class RequestSuratController {
    
    @Autowired
    private RequestService requestService;

    @Autowired
    private TemplateService templateService;

    @GetMapping("request-surat")
    public String formRequestSurat(Model model){

        Map<Integer, String> listBentukSurat = requestService.listBentukSurat();
        model.addAttribute("listBentukSurat", listBentukSurat);

        Map<Integer, String> listBahasa = requestService.listBahasa();
        model.addAttribute("listBahasa", listBahasa);
       
        Map<String, List<String>> jenisSuratByKategori = requestService.generateJenisSuratByKategori();
        model.addAttribute("jenisSuratByKategori", jenisSuratByKategori);

        Map<Integer, String> listKategori = requestService.listKategori();
        model.addAttribute("listKategori", listKategori);

        model.addAttribute("requestTemplate", new RequestTemplate());
       
        model.addAttribute("requestSurat", new RequestSurat());

        return "request-surat";
    }

    @PostMapping("request-surat")
    public String requestSurat(@Valid @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ModelAttribute RequestSurat requestSurat, 
                                    @Valid @ModelAttribute RequestTemplate requestTemplate,
                                    BindingResult suratBindingResult,
                                    BindingResult templateBindingResult, Model model) {
        if (suratBindingResult.hasErrors() || templateBindingResult.hasErrors()) {
            // Ambil semua pesan kesalahan
            List<ObjectError> suratErrors = suratBindingResult.getAllErrors();
            List<ObjectError> templateErrors = templateBindingResult.getAllErrors();
    
            // Gabungkan semua pesan kesalahan menjadi satu
            List<ObjectError> allErrors = new ArrayList<>();
            allErrors.addAll(suratErrors);
            allErrors.addAll(templateErrors);
    
            // Masukkan pesan kesalahan ke dalam model
            model.addAttribute("errorMessages", allErrors);
    
            // Kembalikan pengguna ke halaman form dengan pesan kesalahan
            return "request-surat"; // Nama template HTML yang menampilkan form request surat
        }
        try {
            requestSurat.setTanggalPengajuan(new Date());

            requestService.createRequestTemplate(requestTemplate);
            requestService.createRequestSurat(requestSurat);
            System.out.println("BERHASIL");
            return "redirect:/request-surat"; // Redirect ke halaman history jika berhasil
        } catch (Exception e) {
            // Tangkap dan tangani kesalahan jika terjadi
            e.printStackTrace();
            model.addAttribute("errorMessage", "Gagal membuat permintaan surat: " + e.getMessage());
            return "ERROR";
        }
    }

    public Pengguna createDummyPengguna(Role role) {
        Pengguna dummyPengguna = new Pengguna();
        switch (role) {
            case DOSEN:
                dummyPengguna.setId("1989897777");
                dummyPengguna.setEmail("dosen@example.com");
                dummyPengguna.setNama("Dummy Dosen");
                dummyPengguna.setRole(Role.DOSEN);
                break;
            case STAF:
                dummyPengguna.setId("9800234722");
                dummyPengguna.setEmail("staf@example.com");
                dummyPengguna.setNama("Dummy Staf");
                dummyPengguna.setRole(Role.STAF);
                break;
            case MAHASISWA:
                dummyPengguna.setId("2106751436");
                dummyPengguna.setEmail("mahasiswa@example.com");
                dummyPengguna.setNama("Dummy Mahasiswa");
                dummyPengguna.setRole(Role.MAHASISWA);
                break;
            default:
                break;
        }
        return dummyPengguna;
    }

    @GetMapping("all")
    public ResponseEntity<List<RequestSurat>> showAllRequest() {
        List<RequestSurat> allRequests = requestService.getAllRequestsSurat();
        return new ResponseEntity<>(allRequests, HttpStatus.OK);
    }

    // INI BELUM YAKIN, NYAMBUNGINNYA GIMANA KALO UDH KEINTEGRASI
    // gimana biar nyambung ama IDnya?
    
    // @GetMapping("/by-user")
    // 
    // public ResponseEntity<List<RequestSurat>> showRequestByUser(@RequestBody Pengguna pengguna) {
    //     List<RequestSurat> userRequests = requestService.getRequestsByUser(pengguna);
    //     return new ResponseEntity<>(userRequests, HttpStatus.OK);
    // }

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

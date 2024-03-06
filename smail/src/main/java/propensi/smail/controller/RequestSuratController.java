package propensi.smail.controller;

import org.springframework.web.bind.annotation.*;

import propensi.smail.model.Pengguna;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.TemplateSurat;
import propensi.smail.model.Role;
import propensi.smail.service.RequestService;
import propensi.smail.service.TemplateService;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/request-surat")
public class RequestSuratController {
    
    @Autowired
    private RequestService requestService;

    @Autowired
    private TemplateService templateService;

    @PostMapping("/create-request")
    public ResponseEntity<RequestSurat> createRequest(@RequestBody RequestSurat requestSurat) {
        // try {
            // Pengguna pengaju = requestSurat.getPengaju();

            // // Periksa apakah pengaju telah diinisialisasi
            // if (pengaju == null) {
            //     throw new IllegalArgumentException("Pengaju must be provided");
            // }

            // // Periksa apakah pengaju memiliki role yang valid
            // Role role = pengaju.getRole();
            // if (role == null) {
            //     throw new IllegalArgumentException("Role must be provided for pengaju");
            // }
            // // Buat dummy pengguna jika pengaju tidak memiliki ID (misalnya saat pengguna baru)
            // if (pengaju.getId() == null) {
            //     pengaju = createDummyPengguna(role);
            //     requestSurat.setPengaju(pengaju); // Inisialisasi pengaju dengan dummy pengguna
            // }

            // Mendapatkan daftar template surat dari layanan TemplateService
            // List<TemplateSurat> templates = templateService.showAllTemplates();
            String bentukSurat = requestSurat.getBentukSurat();
            String bahasa = requestSurat.getBahasa();
            // String requestId = requestService.generateRequestId(pengaju);
            // requestSurat.setId(requestId);
            requestSurat.setBentukSurat(bentukSurat);
            requestSurat.setBahasa(bahasa);

            RequestSurat createdRequestSurat = requestService.createRequestSurat(requestSurat);
            return new ResponseEntity<>(createdRequestSurat, HttpStatus.CREATED);
        // } catch (IllegalArgumentException e) {
        //     // Tangani jika nilai string role tidak valid untuk enum Role
        //     return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        // } catch (Exception e) {
        //     return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        // }
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

    @GetMapping("/all")
    public ResponseEntity<List<RequestSurat>> showAllRequest() {
        List<RequestSurat> allRequests = requestService.getAllRequests();
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
}

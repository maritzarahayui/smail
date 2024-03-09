package propensi.smail.service;

import org.springframework.stereotype.Service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;

import propensi.smail.model.Pengguna;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.RequestTemplate;
import propensi.smail.model.Role;
import propensi.smail.model.TemplateSurat;
import propensi.smail.repository.RequestSuratDb;
import propensi.smail.repository.RequestTemplateDb;
import propensi.smail.repository.TemplateSuratDb;

@Service
public class RequestServiceImpl implements RequestService {
    @Autowired
    private RequestSuratDb requestSuratDb;

    @Autowired
    private RequestTemplateDb requestTemplateDb;

    @Autowired
    TemplateSuratDb templateSuratDb;

    @Autowired
    TemplateService templateService;

    @Override
    public RequestSurat createRequestSurat(RequestSurat requestSurat) {
        try {
            requestSurat.setBentukSurat(requestSurat.getBentukSurat());
            requestSurat.setBahasa(requestSurat.getBahasa());
            requestSurat.setKategori(requestSurat.getKategori());
            requestSurat.setJenisSurat(requestSurat.getJenisSurat());
            requestSurat.setStatus(0); // 0 --> REQUESTED

            System.out.println("RequestTemplate saved successfully");
            return requestSuratDb.save(requestSurat);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error saving RequestTemplate");
            return null;
        }
        
    }

    @Override
    public List<RequestSurat> getAllRequestsSurat() {
        return requestSuratDb.findAll();
    }

    // @Override
    // public List<RequestSurat> getRequestsByUser(Pengguna pengguna) {
    //     return requestSuratDb.findByPengaju(pengguna);
    // }

    @Override
    public RequestSurat getRequestSuratById(String requestSuratId) {
        Optional<RequestSurat> requestSurat = requestSuratDb.findById(requestSuratId);
        return requestSurat.orElseThrow(() -> new NoSuchElementException("RequestSurat with id: " + requestSuratId + " not found"));
    }

    @Override
    public RequestSurat batalkanRequestSurat(String requestSuratId) {
        RequestSurat requestSurat = getRequestSuratById(requestSuratId);
        requestSurat.setStatus(1); // Misalnya, 1 mewakili status "Dibatalkan"
        return requestSuratDb.save(requestSurat);
    }

    @Override
    public int countAllRequests() {
        List<RequestSurat> allRequests = getAllRequestsSurat();
        return allRequests.size();
    }


    // @Override
    // public String generateRequestId(Pengguna dummyPengguna) {
    //     long totalRequestsLong = requestSuratDb.countByPengajuRole(dummyPengguna.getRole());
    //     int totalRequests = Math.toIntExact(totalRequestsLong);
    //     String prefix = "";

    //     switch (dummyPengguna.getRole()) {
    //         case DOSEN:
    //             prefix = "DOS";
    //             break;
    //         case STAF:
    //             prefix = "STF";
    //             break;
    //         case MAHASISWA:
    //             prefix = "MHS";
    //             break;
    //         default:
    //             break;
    //     }

    //     return prefix + String.format("%03d", totalRequests + 1);
    // }

    @Override
    public Map<String, List<String>> generateJenisSuratByKategori() {
        List<TemplateSurat> templateSuratList = templateSuratDb.findAll();
        Map<String, List<String>> kategoriJenisMap = new HashMap<>();

        // Loop through the templateSuratList
        for (TemplateSurat template : templateSuratList) {
            String kategori = template.getKategori();
            String jenis = template.getNamaTemplate();

            // If the category already exists in the map, add the type to its list
            if (kategoriJenisMap.containsKey(kategori)) {
                kategoriJenisMap.get(kategori).add(jenis);
            } else { // Otherwise, create a new list for the category and add the type to it
                List<String> jenisList = new ArrayList<>();
                jenisList.add(jenis);
                kategoriJenisMap.put(kategori, jenisList);
            }
        }

        return kategoriJenisMap;
    }
    
    @Override
    public Map<Integer, String> listBahasa(){
        Map<Integer, String> bahasa = new HashMap<>();

        bahasa.put(1, "Indonesia (IND)");
        bahasa.put(2, "Inggris (EN)");

        return bahasa;
    }

    @Override
    public Map<Integer, String> listBentukSurat(){
        Map<Integer, String> bentuk = new HashMap<>();

        bentuk.put(1, "Soft Copy");
        bentuk.put(2, "Hard Copy");

        return bentuk;
    }

    // ------------------REQUEST TEMPLATE----------------
    @Override
    public void createRequestTemplate(RequestTemplate requestTemplate){
        try {
            requestTemplate.setKategori(requestTemplate.getKategori());
            requestTemplate.setBahasa(requestTemplate.getBahasa());
            requestTemplate.setKeperluan(requestTemplate.getKeperluan());

            requestTemplateDb.save(requestTemplate);

            System.out.println("RequestTemplate saved successfully");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error saving RequestTemplate");
        }
    }

    @Override
    public List<RequestTemplate> retrieveAllRequest(){
        return requestTemplateDb.findAll();
    }

    @Override
    public Map<Integer, String> listKategori(){
        Map<Integer, String> kategori = new HashMap<>();

        kategori.put(1, "LEGAL");
        kategori.put(2, "SDM");
        kategori.put(3, "KEUANGAN");
        kategori.put(4, "SARANA");
        kategori.put(5, "KEMAHASISWAAN");
        return kategori;
    }

    // ------PREVIEW-----
    @Override
    public List<String> getAllJenisByKategori(String kategori) {
        return templateSuratDb.findNamaTemplateByKategori(kategori);
    }
}


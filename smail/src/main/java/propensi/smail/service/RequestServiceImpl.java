package propensi.smail.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import propensi.smail.model.*;
import propensi.smail.repository.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;

import propensi.smail.model.user.*;
import propensi.smail.dto.RequestAndFieldDataDTO;
import propensi.smail.model.RequestTemplate;
import propensi.smail.repository.RequestTemplateDb;

@Service
public class RequestServiceImpl implements RequestService {
    @Autowired
    private RequestSuratDb requestSuratDb;

    @Autowired
    private RequestTemplateDb requestTemplateDb;

    @Autowired
    TemplateSuratDb templateSuratDb;

    @Autowired
    SuratKeluarDb suratKeluarDb;

    @Autowired
    TemplateService templateService;

    @Override
    public void saveOrUpdate(RequestSurat requestSurat) {
        requestSuratDb.save(requestSurat);
    }

    @Override
    public RequestSurat createRequestSurat(RequestSurat requestSurat, RequestAndFieldDataDTO requestDTO) {
        try {
            requestSurat.setBentukSurat(requestDTO.getBentukSurat());
            requestSurat.setBahasa(requestDTO.getBahasa());
            requestSurat.setKategori(requestDTO.getKategori());
            requestSurat.setJenisSurat(requestDTO.getJenisSurat());
            requestSurat.setKeperluan(requestDTO.getKeperluan());
            requestSurat.setStatus(1); // Diajukan
            requestSurat.setId(generateRequestId(requestSurat.getPengaju()));
            requestSurat.setListFieldData(requestDTO.getListFieldData());

            for (FieldData fieldData : requestDTO.getListFieldData()) {
                fieldData.setRequestSurat(requestSurat);
            } 

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

    @Override
    public List<RequestSurat> getAllSubmitedRequestsSurat() {
        return requestSuratDb.findByStatus(1);
    }

    @Override
    public List<RequestSurat> getAllCanceledRequestsSurat() {
        return requestSuratDb.findByStatus(2);
    }

    @Override
    public List<RequestSurat> getAllRejectedRequestsSurat() {
        return requestSuratDb.findByStatus(3);
    }

    @Override
    public List<RequestSurat> getAllOnProcessRequestsSurat() {

        return requestSuratDb.findByStatus(4);

    }

    @Override
    public List<RequestSurat> getAllFinishedRequestsSurat() {
        return requestSuratDb.findByStatus(5);
    }

    @Override
    public RequestSurat getRequestSuratById(String requestSuratId) {
        Optional<RequestSurat> requestSurat = requestSuratDb.findById(requestSuratId);
        return requestSurat.orElseThrow(() -> new NoSuchElementException("RequestSurat with id: " + requestSuratId + " not found"));
    }

    // @Override
    // public RequestSurat batalkanRequestSurat(String requestSuratId) {
    //     RequestSurat requestSurat = getRequestSuratById(requestSuratId);
    //     requestSurat.setStatus(2); // Dibatalkan
    //     return requestSuratDb.save(requestSurat);
    // }

    @Override
    public RequestSurat batalkanRequestSurat(String requestSuratId, String alasanPembatalan) {
        RequestSurat requestSurat = getRequestSuratById(requestSuratId);
        requestSurat.setStatus(2); // Dibatalkan
        requestSurat.setAlasanPembatalan(alasanPembatalan);
        return requestSuratDb.save(requestSurat);
    }

    @Override
    public int countAllRequests() {
        List<RequestSurat> allRequests = getAllRequestsSurat();
        return allRequests.size();
    }

    @Override
    public String generateRequestId(Pengguna pengaju) {
        Long totalRequestsBy = (long) 0;
        String prefix = "";

        if (pengaju instanceof Dosen) {
            totalRequestsBy = requestSuratDb.countRequestByDosen();
            prefix = "DOS";
        } else if (pengaju instanceof Staf) {
            totalRequestsBy = requestSuratDb.countRequestByStaf();
            prefix = "STF";
        } else if (pengaju instanceof Mahasiswa) {
            totalRequestsBy = requestSuratDb.countRequestByMahasiswa();
            prefix = "MHS";
        }

        return prefix + String.format("%03d", totalRequestsBy + 1);
    }

    @Override
    public Map<String, List<String>> generateJenisSuratByKategoriAndRole(String tipePengaju) {
        List<TemplateSurat> templateSuratList = templateSuratDb.findAll();
        Map<String, List<String>> kategoriJenisMap = new HashMap<>();

        // Loop through the templateSuratList
        for (TemplateSurat template : templateSuratList) {

            if (template.getListPengguna().contains(tipePengaju)) {
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

    @Override
    public List<RequestSurat> getAllSubmittedRequestsSuratByPengaju(String penggunaId) {
        return requestSuratDb.findByStatusAndPengajuId(1, penggunaId);
    }

    @Override
    public List<RequestSurat> getAllCancelledRequestsSuratByPengaju(String penggunaId) {
        return requestSuratDb.findByStatusAndPengajuId(2, penggunaId);
    }

    @Override
    public List<RequestSurat> getAllRejectedRequestsSuratByPengaju(String penggunaId) {
        return requestSuratDb.findByStatusAndPengajuId(3, penggunaId);
    }

    @Override
    public List<RequestSurat> getAllOnProcessRequestsSuratByPengaju(String penggunaId) {
        return requestSuratDb.findByStatusAndPengajuId(4, penggunaId);
    }

    @Override
    public List<RequestSurat> getAllFinishedRequestsSuratByPengaju(String penggunaId) {
        return requestSuratDb.findByStatusAndPengajuId(5, penggunaId);
    }

    @Override
    public List<RequestSurat> getAllRequestSuratByPenandatanganId(String penandatanganId) {
        // Retrieve all SuratKeluar objects associated with the specified penandatanganId
        List<SuratKeluar> suratKeluarList = suratKeluarDb.findByPenandatanganId(penandatanganId);

        // Create a list to store the associated RequestSurat objects
        List<RequestSurat> requestSuratList = new ArrayList<>();

        // Iterate over the SuratKeluar objects
        for (SuratKeluar suratKeluar : suratKeluarList) {
            // Retrieve the associated RequestSurat object
            RequestSurat requestSurat = suratKeluar.getRequestSurat();

            // Add the retrieved RequestSurat object to the list
            requestSuratList.add(requestSurat);
        }

        // Return the list of associated RequestSurat objects
        return requestSuratList;
    }

    // ------------------REQUEST TEMPLATE----------------
    @Override
    public void createRequestTemplate(RequestTemplate requestTemplate, RequestAndFieldDataDTO requestDTO){
        try {
            requestTemplate.setBahasa(requestDTO.getBahasa());
            requestTemplate.setKategori(requestDTO.getKategori());
            requestTemplate.setStatus(1); // 1 --> REQUESTED
            requestTemplate.setKeperluan(requestDTO.getKeperluan()); // 0 --> REQUESTED
            requestTemplate.setId(generateRequestId(requestTemplate.getPengaju()));
            requestTemplate.setListFieldData(requestDTO.getListFieldData());

            for (FieldData fieldData : requestDTO.getListFieldData()) {
                fieldData.setRequestTemplate(requestTemplate);
            } 

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


package propensi.smail.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import propensi.smail.model.RequestTemplate;
import propensi.smail.repository.RequestTemplateDb;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RequestServiceImpl implements RequestService {
    @Autowired
    private RequestTemplateDb requestTemplateDb;

    @Override
    public void createRequestTemplate(RequestTemplate requestTemplate){
        try {
            requestTemplate.setKategori(requestTemplate.getKategori());
            requestTemplate.setBahasa(requestTemplate.getBahasa());
            requestTemplate.setKeperluan(requestTemplate.getKeperluan());
            requestTemplate.setTanggalPengajuan(new Date());

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

        kategori.put(1, "Keputusan");
        kategori.put(2, "Edaran");
        kategori.put(3, "Keterangan");
        kategori.put(4, "Kuasa");
        kategori.put(5, "Pengantar");
        kategori.put(6, "Perintah");
        kategori.put(7, "Permohonan");
        kategori.put(8, "Lainnya");

        return kategori;
    }

    @Override
    public Map<Integer, String> listBahasa(){
        Map<Integer, String> bahasa = new HashMap<>();

        bahasa.put(1, "Indonesia (IND)");
        bahasa.put(2, "Inggris (EN)");

        return bahasa;
    }
}

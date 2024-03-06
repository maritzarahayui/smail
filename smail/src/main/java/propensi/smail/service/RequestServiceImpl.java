package propensi.smail.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import propensi.smail.model.RequestTemplate;
import propensi.smail.repository.RequestTemplateDb;

import java.util.Date;
import java.util.List;

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
}

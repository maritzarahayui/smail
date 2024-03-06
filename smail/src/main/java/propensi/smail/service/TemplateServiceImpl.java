package propensi.smail.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import propensi.smail.repository.TemplateSuratDb;
import propensi.smail.model.TemplateSurat;;


@Service
public class TemplateServiceImpl implements TemplateService {

    // @Autowired
    // private TemplateSuratDb templateSuratDb;

    // @Override
    // public List<TemplateSurat> showAllTemplates() {
    //     // return templateSuratDb.findByIsActive(true);

    //     // -----SEMENTARA PAKE DUMMY----
    //     // Cek apakah database sudah memiliki data template
    //     List<TemplateSurat> existingTemplates = templateSuratDb.findByIsActive();
    //     if (existingTemplates.isEmpty()) {
    //         // Jika tidak ada data, tambahkan data dummy
    //         List<TemplateSurat> dummyTemplates = createDummyTemplates();
    //         templateSuratDb.saveAll(dummyTemplates);
    //         return dummyTemplates;
    //     } else {
    //         // Jika sudah ada data, kembalikan data yang ada
    //         return existingTemplates;
    //     }
    // }

    // // DUMMYYYYYYY
    // // Method untuk membuat data dummy
    // private List<TemplateSurat> createDummyTemplates() {
    //     List<TemplateSurat> dummyTemplates = new ArrayList<>();

    //     // Template Legal
    //     TemplateSurat legalTemplate = new TemplateSurat();
    //     legalTemplate.setKategori("LEGAL");
    //     legalTemplate.setNamaTemplate("Template Legal");
    //     legalTemplate.setTanggalDibuat(new Date());
    //     legalTemplate.setIsActive(true);
    //     dummyTemplates.add(legalTemplate);

    //     // Template SDM
    //     TemplateSurat sdmTemplate = new TemplateSurat();
    //     sdmTemplate.setKategori("SDM");
    //     sdmTemplate.setNamaTemplate("Template SDM");
    //     sdmTemplate.setTanggalDibuat(new Date());
    //     sdmTemplate.setIsActive(true);
    //     dummyTemplates.add(sdmTemplate);

    //     // Template Keuangan
    //     TemplateSurat keuanganTemplate = new TemplateSurat();
    //     keuanganTemplate.setKategori("KEUANGAN");
    //     keuanganTemplate.setNamaTemplate("Template Keuangan");
    //     keuanganTemplate.setTanggalDibuat(new Date());
    //     keuanganTemplate.setIsActive(true);
    //     dummyTemplates.add(keuanganTemplate);

    //     return dummyTemplates;
    // }


}

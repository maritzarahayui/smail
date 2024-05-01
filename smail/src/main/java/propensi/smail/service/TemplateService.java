package propensi.smail.service;

import org.hibernate.sql.Template;
import org.springframework.web.multipart.MultipartFile;
import propensi.smail.model.RequestTemplate;
import propensi.smail.model.TemplateSurat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface TemplateService {
    TemplateSurat createTemplate(TemplateSurat templateSurat);

    List<TemplateSurat> getAllActiveTemplates();

    TemplateSurat softDeleteTemplate(String templateId);

    List<TemplateSurat> searchTemplatesByNamaTemplate(String namaTemplate);

    TemplateSurat store(MultipartFile file, String kategori, String namaTemplate, ArrayList<String> listPengguna, ArrayList<String> listField);

    TemplateSurat getFile(String id);

    Stream<TemplateSurat> getAllFiles();

    String generateIdByKategori(String kategori);

    TemplateSurat findById(String id);

    TemplateSurat updateTemplate(String id, MultipartFile file, String kategori, String namaTemplate, ArrayList<String> listPengguna, ArrayList<String> listField);

    List<RequestTemplate> getAllReqTemplate();

    RequestTemplate getRequest(String id);

    RequestTemplate terimaRequest(String requestId);

    RequestTemplate tolakRequest(String requestId);

    Map<String, Long> getActiveTemplateByKategori();
}


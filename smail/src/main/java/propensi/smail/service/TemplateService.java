package propensi.smail.service;

import jakarta.mail.MessagingException;
import org.hibernate.sql.Template;
import org.springframework.web.multipart.MultipartFile;
import propensi.smail.model.RequestTemplate;
import propensi.smail.model.TemplateSurat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface TemplateService {
    TemplateSurat createTemplate(TemplateSurat templateSurat);

    List<TemplateSurat> getAllActiveTemplates();

    TemplateSurat softDeleteTemplate(String templateId);

    boolean existsByNamaTemplate(String namaTemplate);

    TemplateSurat store(MultipartFile file, String kategori, String namaTemplate, ArrayList<String> listPengguna, ArrayList<String> listField, String requestTemplate);

    TemplateSurat getFile(String id);

    Stream<TemplateSurat> getAllFiles();

    String generateIdByKategori(String kategori);

    TemplateSurat findById(String id);

    TemplateSurat updateTemplate(String id, MultipartFile file, String namaTemplate, ArrayList<String> listPengguna, ArrayList<String> listField);

    List<RequestTemplate> getAllReqTemplate();

    RequestTemplate getRequest(String id);

    RequestTemplate terimaRequest(String requestId);

    RequestTemplate tolakRequest(String requestId);

    Map<String, Long> getActiveTemplateByKategori();

    List<RequestTemplate> getAllRejectedReq();

    List<RequestTemplate> getAllAcceptedReq();

    List<RequestTemplate> getAllFilteredAcceptedReq();

    List<RequestTemplate> getAllRequestedReq();

    RequestTemplate updateRequest(String requestId);

    RequestTemplate findRequest(String id);

    void sendEmailRejection(String to, String subject, String body, RequestTemplate requestTemplate) throws MessagingException, IOException;
}


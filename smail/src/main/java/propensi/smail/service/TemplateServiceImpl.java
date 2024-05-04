package propensi.smail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import propensi.smail.model.RequestTemplate;
import propensi.smail.model.SuratMasuk;
import propensi.smail.model.TemplateSurat;
import propensi.smail.repository.RequestTemplateDb;
import propensi.smail.repository.TemplateSuratDb;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    TemplateSuratDb templateSuratDb;

    @Autowired
    RequestTemplateDb requestTemplateDb;

    @Override
    public TemplateSurat createTemplate(TemplateSurat templateSurat) {
        System.out.println("masuk method createTemplate serv");
        String kategori = templateSurat.getKategori();
        String id = generateIdByKategori(kategori);

        templateSurat.setId(id);
        templateSurat.setTanggalDibuat(new Date());

        return templateSuratDb.save(templateSurat);
    }

    @Override
    @Transactional
    public TemplateSurat store(MultipartFile file, String kategori, String namaTemplate, ArrayList<String> listPengguna, ArrayList<String> listField) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            TemplateSurat templateSurat = new TemplateSurat();
            templateSurat.setId(generateIdByKategori(kategori));
            templateSurat.setTanggalDibuat(new Date());
            templateSurat.setFile(file.getBytes());
            templateSurat.setKategori(kategori);
            templateSurat.setNamaTemplate(namaTemplate);
            templateSurat.setListField(listField);
            templateSurat.setListPengguna(listPengguna);
            templateSurat.setFileName(fileName);
            return templateSuratDb.save(templateSurat);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + fileName, e);
        }
    }

    @Override
    public RequestTemplate getRequest(String id) {
        return requestTemplateDb.findById(id).get();
    }

    @Override
    public TemplateSurat getFile(String id) {
        return templateSuratDb.findById(id).get();
    }

    @Override
    public Stream<TemplateSurat> getAllFiles() {
        return templateSuratDb.findAll().stream();
    }

    @Override
    public List<RequestTemplate> getAllReqTemplate() {
        return requestTemplateDb.findAll();
    }

    @Override
    public List<RequestTemplate> getAllRequestedReq() {return requestTemplateDb.findByStatus(1);}

    @Override
    public List<RequestTemplate> getAllAcceptedReq() {return requestTemplateDb.findByStatus(2);}

    @Override
    public List<RequestTemplate> getAllRejectedReq() {return requestTemplateDb.findByStatus(3);}

    @Override
    public String generateIdByKategori(String kategori) {
        Map<String, String> kategoriMap = Map.of(
                "LEGAL", "LGL",
                "SDM", "SDM",
                "KEUANGAN", "KEU",
                "SARANA", "SAR",
                "KEMAHASISWAAN", "KMH"
        );

        String abbreviation = kategoriMap.get(kategori.toUpperCase());

        if (abbreviation != null) {
            long count = templateSuratDb.countByKategori(kategori);

            String idSuffix = String.format("%03d", count + 1);
            return abbreviation + idSuffix;
        } else {
            throw new IllegalArgumentException("Invalid kategori: " + kategori);
        }
    }

    @Transactional
    public List<TemplateSurat> searchTemplatesByNamaTemplate(String namaTemplate) {
        return templateSuratDb.findByNamaTemplateContainingIgnoreCaseAndIsActiveIsTrue(namaTemplate);
    }


    @Override
    @Transactional
    public List<TemplateSurat> getAllActiveTemplates() {
        return templateSuratDb.findByIsActiveTrue();
    }

    @Override
    public TemplateSurat softDeleteTemplate(String templateId) {
        TemplateSurat template = templateSuratDb.findById(templateId).orElse(null);
        if (template != null) {
            if (template.isActive()) {
                template.setActive(false);
                return templateSuratDb.save(template);
            } else {
                throw new IllegalStateException("Template is already deleted.");
            }
        } else {
            return null; // Or throw an exception indicating the template cannot be found
        }
    }

    @Override
    public RequestTemplate updateRequest(String requestId) {
        RequestTemplate reqTemplate = requestTemplateDb.findById(requestId).orElse(null);
        if (reqTemplate != null) {
            return requestTemplateDb.save(reqTemplate);
        } else {
            return null; // Or throw an exception indicating the template cannot be found
        }
    }

    @Override
    public RequestTemplate terimaRequest(String requestId) {
        RequestTemplate reqTemplate = requestTemplateDb.findById(requestId).orElse(null);
        if (reqTemplate != null) {
            if (reqTemplate.getStatus() == 1) {
                reqTemplate.setStatus(2);
                return requestTemplateDb.save(reqTemplate);
            } else {
                throw new IllegalStateException("Template's status is not Diajukan.");
            }
        } else {
            return null; // Or throw an exception indicating the template cannot be found
        }
    }

    @Override
    public RequestTemplate tolakRequest(String requestId) {
        RequestTemplate reqTemplate = requestTemplateDb.findById(requestId).orElse(null);
        if (reqTemplate != null) {
            if (reqTemplate.getStatus() == 1) {
                reqTemplate.setStatus(3);
                return requestTemplateDb.save(reqTemplate);
            } else {
                throw new IllegalStateException("Template's status is not Diajukan.");
            }
        } else {
            return null; // Or throw an exception indicating the template cannot be found
        }
    }

    @Override
    public TemplateSurat findById(String id) {
        return templateSuratDb.findById(id).get();
    }

    @Override
    public RequestTemplate findRequest(String id) {
        return requestTemplateDb.findById(id).get();
    }

    @Override
    @Transactional
    public TemplateSurat updateTemplate(String id, MultipartFile file, String kategori, String namaTemplate, ArrayList<String> listPengguna, ArrayList<String> listField) {
        TemplateSurat existingTemplate = findById(id);
        if (existingTemplate != null) {
            try {
                // Update properties
                existingTemplate.setKategori(kategori);
                existingTemplate.setNamaTemplate(namaTemplate);
                existingTemplate.setListPengguna(listPengguna);
                existingTemplate.setListField(listField);

                // Check if a new file is uploaded
                if (file != null && !file.isEmpty()) {
                    // Convert the file to byte array
                    byte[] fileBytes = file.getBytes();

                    // Set the file and file name
                    existingTemplate.setFile(fileBytes);
                    existingTemplate.setFileName(file.getOriginalFilename());
                }

                // Save the updated template
                return templateSuratDb.save(existingTemplate);
            } catch (IOException e) {
                throw new RuntimeException("Failed to update file for template with ID " + id, e);
            }
        } else {
            throw new IllegalArgumentException("Template not found with ID: " + id);
        }
    }

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendEmailRejection(String to, String subject, String body, RequestTemplate requestTemplate) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        Date tanggalDibuat = requestTemplate.getTanggalPengajuan();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));


        body = String.format("Yth Bapak/Ibu %s,\n\n" // Include the name of the requester
                        + "Terima kasih atas permintaan yang telah diajukan kepada kami pada tanggal %s untuk keperluan: %s.\n\n" // Include the date of the request and purpose
                        + "Berdasarkan peninjauan admin, kami tidak dapat melanjutkan permintaan template surat dengan ID %s karena alasan berikut:\n\n" // Include the ID
                        + "- %s\n\n" // Include the rejection reason
                        + "Mohon untuk dapat melakukan evaluasi berdasarkan informasi tersebut sebelum mengajukan permintaan baru. Untuk melakukan pengajuan permintaan baru atau pertanyaan terkait pengajuan, silakan kunjungi SMAIL Institut Tazkia melalui tautan berikut: https://smail-rtx.up.railway.app/. Terima kasih atas pengertiannya.\n\n"
                        + "Salam,\n"
                        + "Yayasan Tazkia\n"
                        + "Jl. Ir. H. Djuanda No. 78, Bogor, Jawa Barat 16122\n",
                requestTemplate.getPengaju().getNama(), // Retrieves the name from requestTemplate
                dateFormat.format(tanggalDibuat), // Retrieves the date of the request from requestTemplate
                requestTemplate.getKeperluan(), // Retrieves the purpose of the request from requestTemplate
                requestTemplate.getId(), // Retrieves the ID from requestTemplate
                requestTemplate.getAlasanPenolakan()); // Retrieves the rejection reason from requestTemplate

        helper.setTo(to);
        helper.setSubject("[DITOLAK] Permintaan Template Surat dengan ID " + requestTemplate.getId());
        helper.setText(body, false);
        helper.setFrom("instituttazkia.adm@gmail.com");

        mailSender.send(message);
    }



}

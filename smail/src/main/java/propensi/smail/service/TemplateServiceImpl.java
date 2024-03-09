package propensi.smail.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import propensi.smail.model.TemplateSurat;
import propensi.smail.repository.TemplateSuratDb;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import propensi.smail.repository.TemplateSuratDb;
import propensi.smail.model.TemplateSurat;;


@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    TemplateSuratDb templateSuratDb;

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
    public TemplateSurat getFile(String id) {
        return templateSuratDb.findById(id).get();
    }

    @Override
    public Stream<TemplateSurat> getAllFiles() {
        return templateSuratDb.findAll().stream();
    }


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
    public TemplateSurat findById(String id) {
        return templateSuratDb.findById(id).get();
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



}

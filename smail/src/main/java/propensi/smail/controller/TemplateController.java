package propensi.smail.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import propensi.smail.model.TemplateSurat;
import propensi.smail.service.TemplateService;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/template")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @PostMapping("/add")
    public ResponseEntity<TemplateSurat> createTemplate(@RequestBody TemplateSurat templateSurat) {
        try {
            TemplateSurat createdTemplate = templateService.createTemplate(templateSurat);
            return new ResponseEntity<>(createdTemplate, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("kategori") String kategori, @RequestParam("namaTemplate") String namaTemplate,
                                             @RequestParam("listPengguna") ArrayList<String> listPengguna, @RequestParam("listField") ArrayList<String> listField) throws ParseException {
        String message = "";
        //convert type of tanggalDibuat from String to Date
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//        Date tanggalDibuatDate = formatter.parse(tanggalDibuat);

        try {
            templateService.store(file, kategori, namaTemplate, listField, listPengguna);
            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }
    }


    @GetMapping("/files")
    public ResponseEntity<List<String>> getListFiles() {
        List<String> fileNames = templateService.getAllFiles().map(fileName -> {
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/template/files/").path(fileName.getId()).toUriString();
            return fileDownloadUri;
        }).collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(fileNames);
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable String id) {
        TemplateSurat file = templateService.getFile(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .body(file.getFile());
    }

    @GetMapping("/view-all")
    public ResponseEntity<List<TemplateSurat>> getAllActiveTemplates() {
        try {
            List<TemplateSurat> activeTemplates = templateService.getAllActiveTemplates();
            return new ResponseEntity<>(activeTemplates, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // or log error
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{templateId}/delete")
    public ResponseEntity<?> softDeleteTemplate(@PathVariable String templateId) {
        try {
            TemplateSurat deletedTemplate = templateService.softDeleteTemplate(templateId);
            if (deletedTemplate != null) {
                return ResponseEntity.ok(deletedTemplate);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Template not found");
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // or log error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }






}

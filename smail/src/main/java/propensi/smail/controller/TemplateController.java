package propensi.smail.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import propensi.smail.model.TemplateSurat;
import propensi.smail.service.TemplateService;

import java.util.List;

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

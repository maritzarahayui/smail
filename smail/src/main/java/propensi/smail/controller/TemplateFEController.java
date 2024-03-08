package propensi.smail.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import propensi.smail.model.TemplateSurat;
import propensi.smail.service.TemplateService;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/template")
public class TemplateFEController {

        @Autowired
        private TemplateService templateSuratService;

        @GetMapping("/active-templates")
        public String showActiveTemplates(Model model) {
            List<TemplateSurat> activeTemplates = templateSuratService.getAllActiveTemplates();
            model.addAttribute("activeTemplates", activeTemplates);
            return "active-templates";
        }

        @GetMapping("/search")
        public String searchTemplates(@RequestParam(name = "namaTemplate", required = false) String namaTemplate, Model model) {
            if (namaTemplate != null && !namaTemplate.isEmpty()) {
                List<TemplateSurat> searchResults = templateSuratService.searchTemplatesByNamaTemplate(namaTemplate);
                model.addAttribute("activeTemplates", searchResults);
            } else {
                List<TemplateSurat> activeTemplates = templateSuratService.getAllActiveTemplates();
                model.addAttribute("activeTemplates", activeTemplates);
            }
            return "active-templates";
        }

        @GetMapping("/new-template")
        public String showTambahTemplateForm(Model model) {
            model.addAttribute("templateSurat", new TemplateSurat());
            return "new-template";
        }

        @PostMapping("/new-template")
        public String uploadFile(@RequestParam("file") MultipartFile file,
                                 @RequestParam("kategori") String kategori,
                                 @RequestParam("namaTemplate") String namaTemplate,
                                 @RequestParam("listPengguna") ArrayList<String> listPengguna,
                                 @RequestParam("listField") ArrayList<String> listField,
                                 Model model) {
            String message = "";
            String viewName = "active-templates"; // Define the name of your success view

            try {
                templateSuratService.store(file, kategori, namaTemplate, listField, listPengguna);
                message = "Uploaded the file successfully: " + file.getOriginalFilename();
                model.addAttribute("message", message);
            } catch (Exception e) {
                message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                model.addAttribute("errorMessage", message);
                viewName = "template_error"; // Define the name of your error view
            }

            return viewName;
        }

        @GetMapping("/detail/{id}")
        public String previewPDF(@PathVariable("id") String id, Model model) throws IOException {
            TemplateSurat file = templateSuratService.getFile(id);
            byte[] pdf = file.getFile();

            // Convert PDF content to Base64
            String base64PDF = Base64.getEncoder().encodeToString(pdf);

            model.addAttribute("base64PDF", base64PDF);
            model.addAttribute("template", file); // Add the template object to the model
            return "template-detail"; // Return the PDF preview Thymeleaf template
        }

        @GetMapping("/soft-delete/{id}")
        public String softDeleteTemplate(@PathVariable("id") String templateId, Model model) {
            try {
                TemplateSurat deletedTemplate = templateSuratService.softDeleteTemplate(templateId);
                if (deletedTemplate != null) {
                    model.addAttribute("message", "Template deleted successfully.");
                } else {
                    model.addAttribute("errorMessage", "Template not found or already deleted.");
                }
            } catch (IllegalStateException e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
            return "redirect:/template/active-templates"; // Redirect to the list of active templates
        }

        @GetMapping("/update/{id}")
        public String showUpdateTemplateForm(@PathVariable("id") String id, Model model) {
            // Retrieve the template by ID
            TemplateSurat template = templateSuratService.findById(id);
            if (template == null) {
                throw new IllegalArgumentException("Template not found with ID: " + id);
            }

            // Populate the model with the template data
            model.addAttribute("template", template);
            return "update-template-form"; // Return the update template view
        }

        @PostMapping("/update/{id}")
        public String updateTemplate(@PathVariable("id") String id,
                                     @RequestParam("file") MultipartFile file,
                                     @RequestParam("kategori") String kategori,
                                     @RequestParam("namaTemplate") String namaTemplate,
                                     @RequestParam("listPengguna") ArrayList<String> listPengguna,
                                     @RequestParam("listField") ArrayList<String> listField,
                                     Model model) {
            String message = "";
            String viewName = "active-templates"; // Define the name of your success view

            try {
                // Update the template with the provided data
                templateSuratService.updateTemplate(id, file, kategori, namaTemplate, listPengguna, listField);
                message = "Template updated successfully";
                model.addAttribute("message", message);
            } catch (Exception e) {
                message = "Failed to update the template: " + e.getMessage();
                model.addAttribute("errorMessage", message);
                viewName = "template_error"; // Define the name of your error view
            }

            return viewName;
        }






}








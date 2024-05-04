package propensi.smail.controller;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.RequestTemplate;
import propensi.smail.model.SuratMasuk;
import propensi.smail.model.TemplateSurat;
import propensi.smail.model.user.Pengguna;
import propensi.smail.repository.PenggunaDb;
import propensi.smail.repository.RequestTemplateDb;
import propensi.smail.service.PenggunaService;
import propensi.smail.service.TemplateService;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Controller
@RequestMapping("/template")
public class TemplateFEController {

    @Autowired
    private TemplateService templateSuratService;

    @Autowired
    RequestTemplateDb requestTemplateDb;

    @Autowired
    PenggunaDb penggunaDb;

    @Autowired
    PenggunaService penggunaService;

    @GetMapping("/new-requests")
    public String showTemplateRequests(Model model, Authentication auth) {
        List<RequestTemplate> allRequestTemplates = templateSuratService.getAllReqTemplate();
        List<RequestTemplate> acceptedRequestTemplates = templateSuratService.getAllAcceptedReq();
        List<RequestTemplate> rejectedRequestTemplates = templateSuratService.getAllRejectedReq();
        List<RequestTemplate> requestedRequestTemplates = templateSuratService.getAllRequestedReq();

        model.addAttribute("requestTemplates", allRequestTemplates);
        model.addAttribute("requestedRequests", requestedRequestTemplates);
        model.addAttribute("acceptedRequests", acceptedRequestTemplates);
        model.addAttribute("rejectedRequests", rejectedRequestTemplates);


        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        return "daftar-request-template";
    }

    @Transactional
    @GetMapping("/request/detail/{id}")
    public String showDetailTemplateRequests(@PathVariable("id") String id, Model model, Authentication auth) {
        RequestTemplate requestTemplate = templateSuratService.getRequest(id);

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(2, "Diterima");
        statusMap.put(3, "Ditolak");

        model.addAttribute("statusMap", statusMap);

        model.addAttribute("requestTemplate", requestTemplate); // Add the template object to the model
        return "detail-request-template"; // Return the PDF preview Thymeleaf template
    }

    @GetMapping("/active-templates")
    public String showActiveTemplates(Model model, Authentication auth) {
        List<TemplateSurat> activeTemplates = templateSuratService.getAllActiveTemplates();
        model.addAttribute("activeTemplates", activeTemplates);

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        return "daftar-template";
    }

    @GetMapping("/search")
    public String searchTemplates(@RequestParam(name = "namaTemplate", required = false) String namaTemplate, 
        Model model, Authentication auth) {
        if (namaTemplate != null && !namaTemplate.isEmpty()) {
            List<TemplateSurat> searchResults = templateSuratService.searchTemplatesByNamaTemplate(namaTemplate);
            model.addAttribute("activeTemplates", searchResults);
        } else {
            List<TemplateSurat> activeTemplates = templateSuratService.getAllActiveTemplates();
            model.addAttribute("activeTemplates", activeTemplates);
        }

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }
        return "daftar-template";
    }

    @GetMapping("/new-template")
    public String showTambahTemplateForm(Model model, Authentication auth) {
        model.addAttribute("templateSurat", new TemplateSurat());

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        return "add-template";
    }

    @PostMapping("/new-template")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                                @RequestParam("kategori") String kategori,
                                @RequestParam("namaTemplate") String namaTemplate,
                                @RequestParam("listPengguna") ArrayList<String> listPengguna,
                                @RequestParam("listField") String[] listField,
                                Model model) {
        String message = "";

        try {
            ArrayList<String> newListField = new ArrayList<>(Arrays.asList(listField));
            templateSuratService.store(file, kategori, namaTemplate, listPengguna, newListField);
            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            model.addAttribute("message", message);
            return "redirect:/template/active-templates";
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            model.addAttribute("errorMessage", message);
            return "redirect:/template/active-templates";
        }
    }

    @GetMapping("/detail/{id}")
    public String previewPDF(@PathVariable("id") String id, Model model, Authentication auth) throws IOException {
        TemplateSurat file = templateSuratService.getFile(id);
        byte[] pdf = file.getFile();

        // Convert PDF content to Base64
        String base64PDF = Base64.getEncoder().encodeToString(pdf);

        model.addAttribute("base64PDF", base64PDF);
        model.addAttribute("template", file); // Add the template object to the model

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        return "detail-template"; // Return the PDF preview Thymeleaf template
    }

    @GetMapping("/soft-delete/{id}")
    public String softDeleteTemplate(@PathVariable("id") String templateId, Model model, Authentication auth) {
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

    @PostMapping("/request/detail/{id}/updateStatus")
    public String updateStatus(@PathVariable("id") String id, @RequestParam("status") int status, @RequestParam(value = "alasanPenolakan", required = false) String alasanPenolakan,
                               Model model, Authentication auth) {
        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }

        RequestTemplate requestTemplate = templateSuratService.getRequest(id);
        requestTemplate.setStatus(status);

        if (status == 3) {
            requestTemplate.setAlasanPenolakan(alasanPenolakan);

            try {
                templateSuratService.sendEmailRejection(requestTemplate.getPengaju().getEmail(), "", "", requestTemplate);
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            requestTemplate.setAlasanPenolakan(null);
        }

        templateSuratService.updateRequest(requestTemplate.getId());

        return "redirect:/template/request/detail/{id}";
    }



    @GetMapping("/update/{id}")
    public String showUpdateTemplateForm(@PathVariable("id") String id, Model model, Authentication auth) {
        // Retrieve the template by ID
        TemplateSurat template = templateSuratService.findById(id);

        if (template == null) {
            throw new IllegalArgumentException("Template not found with ID: " + id);
        }

        // Populate the model with the template data
        model.addAttribute("template", template);
//            model.addAttribute("base64PDF", base64PDF);

        if (auth != null) {
            OidcUser oauthUser = (OidcUser) auth.getPrincipal();
            String email = oauthUser.getEmail();
            Optional<Pengguna> user = penggunaDb.findByEmail(email);

            if (user.isPresent()) {
                Pengguna pengguna = user.get();
                model.addAttribute("role", penggunaService.getRole(pengguna));
                model.addAttribute("namaDepan", penggunaService.getFirstName(pengguna));
            } else {
                return "auth-failed";
            }
        }
        
        return "update-template-form"; // Return the update template view
    }

    @PostMapping("/update/{id}")
    public String updateTemplate(@PathVariable("id") String id,
                                    @RequestParam("file") MultipartFile file,
                                    @RequestParam("kategori") String kategori,
                                    @RequestParam("namaTemplate") String namaTemplate,
                                    @RequestParam("listPengguna") ArrayList<String> listPengguna,
                                    @RequestParam("listField") ArrayList<String> listField,
                                    Model model, Authentication auth) {
        String message = "";

        try {
            // Update the template with the provided data
            templateSuratService.updateTemplate(id, file, kategori, namaTemplate, listPengguna, listField);
            message = "Template updated successfully";
            model.addAttribute("message", message);
            return "redirect:/template/detail/{id}";
        } catch (Exception e) {
            message = "Failed to update the template: " + e.getMessage();
            model.addAttribute("errorMessage", message);
            return "redirect:/template/detail/{id}";
        }
    }

}








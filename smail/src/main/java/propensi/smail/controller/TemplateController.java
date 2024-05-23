package propensi.smail.controller;

import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

import propensi.smail.model.*;
import propensi.smail.model.user.*;
import propensi.smail.repository.*;
import propensi.smail.service.*;

import java.io.IOException;
import java.util.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Controller
@RequestMapping("/template")
public class TemplateController {

    @Autowired
    private TemplateService templateSuratService;

    @Autowired
    RequestTemplateDb requestTemplateDb;

    @Autowired
    TemplateSuratDb templateSuratDb;

    @Autowired
    PenggunaDb penggunaDb;

    @Autowired
    PenggunaService penggunaService;

    @Autowired
    private RequestService requestService;

    @GetMapping("/requests")
    public String showTemplateRequests(@RequestParam(name = "keyword", required = false) String keyword, Model model, 
        Authentication auth, @RequestParam(required = false) String activeTab) {
        List<RequestTemplate> allRequestTemplates = new ArrayList<>();
        List<RequestTemplate> acceptedRequestTemplates = new ArrayList<>();
        List<RequestTemplate> rejectedRequestTemplates = new ArrayList<>();
        List<RequestTemplate> requestedRequestTemplates = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            allRequestTemplates = requestTemplateDb.findByKeyword(keyword);
            acceptedRequestTemplates = templateSuratService.searchRequests(keyword, 2);
            rejectedRequestTemplates = templateSuratService.searchRequests(keyword, 3);
            requestedRequestTemplates = templateSuratService.searchRequests(keyword, 1);
        } else {
            allRequestTemplates = templateSuratService.getAllReqTemplate();
            acceptedRequestTemplates = templateSuratService.getAllAcceptedReq();
            rejectedRequestTemplates = templateSuratService.getAllRejectedReq();
            requestedRequestTemplates = templateSuratService.getAllRequestedReq();
        }

        model.addAttribute("requestTemplates", allRequestTemplates);
        model.addAttribute("requestedRequests", requestedRequestTemplates);
        model.addAttribute("acceptedRequests", acceptedRequestTemplates);
        model.addAttribute("rejectedRequests", rejectedRequestTemplates);
        model.addAttribute("activeTab", activeTab != null ? activeTab : "#semua");

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

    @Transactional(readOnly = true)
    @GetMapping("/request/detail/{id}")
    public String showDetailTemplateRequests(@PathVariable("id") String id, Model model, Authentication auth) {
        RequestTemplate requestTemplate = templateSuratService.getRequest(id);

        RequestTemplate file = requestService.getFile(id);

        if (file != null) {
            byte[] pdf = file.getFile();

            if (pdf != null) {
                String base64PDF = Base64.getEncoder().encodeToString(pdf);
                model.addAttribute("base64PDF", base64PDF);
                model.addAttribute("template", file);
            }
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

        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(2, "Diterima");
        statusMap.put(3, "Ditolak");

        model.addAttribute("statusMap", statusMap);

        model.addAttribute("requestTemplate", requestTemplate); // Add the template object to the model
        return "detail-request-template"; // Return the PDF preview Thymeleaf template
    }

    @GetMapping("")
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

    @GetMapping("/create")
    public String showTambahTemplateForm(Model model, Authentication auth) {
        model.addAttribute("templateSurat", new TemplateSurat());

        List<RequestTemplate> requestTemplates = templateSuratService.getAllFilteredAcceptedReq();
        model.addAttribute("requestTemplates", requestTemplates);

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

    @PostMapping("/create")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam("kategori") String kategori,
                             @RequestParam("namaTemplate") String namaTemplate,
                             @RequestParam("listPengguna") ArrayList<String> listPengguna,
                             @RequestParam("listField[]") String[] listField,
                             @RequestParam(value = "requestTemplate", required = false) String requestTemplateId,
                             Model model,
                             Authentication auth) {
        String message = "";

        List<RequestTemplate> requestTemplates = templateSuratService.getAllFilteredAcceptedReq();
        model.addAttribute("requestTemplates", requestTemplates);

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

        try {
            ArrayList<String> newListField = new ArrayList<>(Arrays.asList(listField));

            String requestTemplate = null;
            if (requestTemplateId != null && !requestTemplateId.isEmpty()) {
                requestTemplate = requestTemplateId;
            }

            if (templateSuratDb.findByNamaTemplate(namaTemplate) != null) {

                message = "Template dengan nama " + namaTemplate + " sudah ada.";
                model.addAttribute("errorMessage", message);
                return "add-template";
            } else {
                templateSuratService.store(file, kategori, namaTemplate, listPengguna, newListField, requestTemplate);

                message = "Uploaded the file successfully: " + file.getOriginalFilename();
                model.addAttribute("message", message);
            }
            return "redirect:/template";
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            model.addAttribute("errorMessage", message);
            return "template-form";
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

        TemplateSurat templateSurat = templateSuratService.findById(id);

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
        return "redirect:/template"; // Redirect to the list of active templates
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

            RequestSurat requestSurat = requestService.getRequestSuratById(id);
            if (requestSurat != null) {
                requestSurat.setStatus(3); // Set status to 'rejected'
                requestSurat.setAlasanPenolakan(alasanPenolakan);
                requestSurat.setTanggalPenolakan(new Date());
                requestService.saveOrUpdate(requestSurat); // Update the RequestSurat in the database
            }

            try {
                templateSuratService.sendEmailRejection(requestTemplate.getPengaju().getEmail(), "", "", requestTemplate);
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (status == 2) {

            RequestSurat requestSurat = requestService.getRequestSuratById(id);
            if (requestSurat != null) {
                requestSurat.setStatus(4); // Set status to 'rejected'
                requestService.saveOrUpdate(requestSurat); // Update the RequestSurat in the database
            }

        }else {
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
                                 @RequestParam("namaTemplate") String namaTemplate,
                                 @RequestParam("listPengguna") ArrayList<String> listPengguna,
                                 @RequestParam("listField[]") ArrayList<String> listField,
                                 Model model, Authentication auth) {
        String message = "";

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

        try {
            TemplateSurat template = templateSuratService.findById(id);
            TemplateSurat existingTemplate = templateSuratDb.findByNamaTemplate(namaTemplate);
            if (existingTemplate != null && !existingTemplate.getId().equals(id)) {
                message = "Template dengan nama " + namaTemplate + " sudah ada.";
                model.addAttribute("errorMessage", message);
                model.addAttribute("template", template);
                return "update-template-form";
            } else {
                // Update the template with the provided data
                templateSuratService.updateTemplate(id, file, namaTemplate, listPengguna, listField);
                message = "Template updated successfully";
                model.addAttribute("message", message);
                return "redirect:/template/detail/" + id;
            }
        } catch (Exception e) {
            message = "Failed to update the template: " + e.getMessage();
            model.addAttribute("errorMessage", message);
            return "update-template-form";
        }
    }

}








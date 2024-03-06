package propensi.smail.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import propensi.smail.model.RequestTemplate;
import propensi.smail.service.RequestService;

@Controller
public class RequestTemplateController {
    @Autowired
    private RequestService requestService;

    @GetMapping("request-template")
    public String formRequestTemplate(Model model){
        model.addAttribute("requestTemplate", new RequestTemplate());
        return "request-surat";
    }

    @PostMapping("request-template")
    public String requestTemplate(@Valid @ModelAttribute RequestTemplate requestTemplate,
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) BindingResult bindingResult) {
        requestService.createRequestTemplate(requestTemplate);
        System.out.println("BERHASIL");
        return "request-surat";
    }
}

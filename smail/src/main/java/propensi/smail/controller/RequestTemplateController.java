package propensi.smail.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import propensi.smail.model.RequestTemplate;
import propensi.smail.repository.PenggunaDb;
import propensi.smail.repository.RequestTemplateDb;
import propensi.smail.service.PenggunaService;
import propensi.smail.service.RequestService;

import java.util.Map;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Controller
public class RequestTemplateController {
    @Autowired
    private RequestTemplateDb requestTemplateDb;

    @Autowired
    private RequestService requestService;

    @Autowired
    PenggunaDb penggunaDb;

    @Autowired
    PenggunaService penggunaService;

    @GetMapping(value = "/allTemplate")
    private List<RequestTemplate> retrieveAllRequest(){
        return requestService.retrieveAllRequest();
    }
}

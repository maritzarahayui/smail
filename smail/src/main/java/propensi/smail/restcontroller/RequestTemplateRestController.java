package propensi.smail.restcontroller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import propensi.smail.model.RequestTemplate;
import propensi.smail.repository.RequestTemplateDb;
import propensi.smail.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/api/request-template")
public class RequestTemplateRestController {
    @Autowired
    private RequestTemplateDb requestTemplateDb;

    @Autowired
    private RequestService requestService;

    @PostMapping(value = "/new")
    private RequestTemplate restRequestTemplate(@Valid @RequestBody RequestTemplate requestTemplate, BindingResult bindingResult){
        if(bindingResult.hasFieldErrors()){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Request body has invalid type or missing field"
            );
        } else {
            requestService.createRequestTemplate(requestTemplate);
            return requestTemplate;
        }
    }

    @GetMapping(value = "/all")
    private List<RequestTemplate> retrieveAllRequest(){
        return requestService.retrieveAllRequest();
    }
}

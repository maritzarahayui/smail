package propensi.smail.service;

import propensi.smail.model.RequestTemplate;

import java.util.List;

public interface RequestService {
    void createRequestTemplate(RequestTemplate requestTemplate);
    List<RequestTemplate> retrieveAllRequest();
}

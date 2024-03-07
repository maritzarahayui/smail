package propensi.smail.service;

import propensi.smail.model.RequestTemplate;

import java.util.List;
import java.util.Map;

public interface RequestService {
    void createRequestTemplate(RequestTemplate requestTemplate);
    List<RequestTemplate> retrieveAllRequest();
    Map<Integer, String> listKategori();
    Map<Integer, String> listBahasa();
}

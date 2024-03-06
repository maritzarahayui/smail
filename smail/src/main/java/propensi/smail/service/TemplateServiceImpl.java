package propensi.smail.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import propensi.smail.model.TemplateSurat;
import propensi.smail.repository.TemplateSuratDb;

import java.util.List;
import java.util.Optional;

@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    TemplateSuratDb templateSuratDb;


    @Override
    public TemplateSurat createTemplate (TemplateSurat templateSurat) {
        return templateSuratDb.save(templateSurat);
    }

    @Override
    public List<TemplateSurat> getAllActiveTemplates() {
        return templateSuratDb.findByIsActiveTrue();
    }

    @Override
    public TemplateSurat softDeleteTemplate(String templateId) {
        TemplateSurat template = templateSuratDb.findById(templateId).orElse(null);
        if (template != null) {
            if (template.isActive()) {
                template.setActive(false);
                return templateSuratDb.save(template);
            } else {
                throw new IllegalStateException("Template is already deleted.");
            }
        } else {
            return null; // Or throw an exception indicating the template cannot be found
        }
    }


}

package propensi.smail.service;

import org.hibernate.sql.Template;
import propensi.smail.model.TemplateSurat;

import java.util.List;

public interface TemplateService {
    TemplateSurat createTemplate(TemplateSurat templateSurat);
    List<TemplateSurat> getAllActiveTemplates();
    TemplateSurat softDeleteTemplate(String templateId);
}

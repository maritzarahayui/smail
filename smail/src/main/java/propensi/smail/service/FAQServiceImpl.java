package propensi.smail.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import propensi.smail.model.FAQ;
import propensi.smail.repository.FAQDb;

@Service
public class FAQServiceImpl implements FAQService {

    @Autowired
    private FAQDb faqDb;

    @Override
    public void createFAQ(FAQ faq) {
        faq.setStatus(0);
        faqDb.save(faq);
    }

    @Override
    public FAQ jawabFAQ(FAQ newFaq) {
        FAQ faq = getFAQbyId(newFaq.getId());
        faq.setJawaban(newFaq.getJawaban());
        faq.setStatus(2);
        faqDb.save(faq);
        return faq;
    }

    @Override
    public List<FAQ> getFaqsByStatus(int status) {
        return faqDb.findByStatus(status);
    }

    @Override
    public FAQ getFAQbyId(int idFAQ) {
        return faqDb.findById(idFAQ);
    }

    @Override
    public FAQ eskalasiFAQ(int idFAQ) {
        FAQ faq = getFAQbyId(idFAQ);
        faq.setStatus(1);
        faqDb.save(faq);
        return faq;
    }

    @Override
    public FAQ deleteFAQ(int idFAQ) {
        FAQ faq = getFAQbyId(idFAQ);
        faq.setStatus(3);
        faqDb.save(faq);
        return faq;
    }

    @Override
    public FAQ editFAQ(FAQ newFaq) {
        FAQ faq = getFAQbyId(newFaq.getId());
        faq.setJawaban(newFaq.getJawaban());
        faqDb.save(faq);
        return faq;
    }
    
}

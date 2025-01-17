package propensi.smail.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import propensi.smail.model.*;
import propensi.smail.model.user.*;
import propensi.smail.repository.FAQDb;

@Service
public class FAQServiceImpl implements FAQService {

    @Autowired
    private FAQDb faqDb;

    @Override
    public void createFAQ(FAQ faq, Pengguna sender) {
        faq.setPengaju(sender);
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

    @Override
    public List<FAQ> getFaqsByStatusAndSearch(String search, int status) {
        return faqDb.findByPertanyaanContainingIgnoreCaseAndStatus(search, status) ;
    }

    @Override
    public List<FAQ> getAllNotAnsweredFaq() {
        return faqDb.findByStatus(0);
    }

    @Override
    public List<FAQ> getAllEskalasiFaq() {
        return faqDb.findByStatus(1);
    }

    @Override
    public List<FAQ> getAllAnsweredFaq() {
        return faqDb.findByStatus(2);
    }

    @Override
    public List<FAQ> getAllDeletedFaq() {
        return faqDb.findByStatus(3);
    }
    @Override
    public Map<String, Long> getCountOfAnsweredQuestions(Pengguna pengaju) {
        Map<String, Long> terjawab = new HashMap<>();
        long jumlahBelumTerjawab = faqDb.countFAQByPengajuAndStatus(pengaju, 0);
        long jumlahTerjawab = faqDb.countFAQByPengajuAndStatus(pengaju, 2);
        terjawab.put("BelumTerjawab", jumlahBelumTerjawab);
        terjawab.put("Terjawab", jumlahTerjawab);
        return terjawab;
    }

    @Override
    public List<FAQ> getFaqsByPengajuAndStatus(Pengguna pengaju, int status) {
        return faqDb.findByPengajuAndStatus(pengaju, status);
    }
}

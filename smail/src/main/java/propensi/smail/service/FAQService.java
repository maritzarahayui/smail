package propensi.smail.service;

import propensi.smail.model.FAQ;
import propensi.smail.model.user.Pengguna;

import java.util.*;

public interface FAQService {
    void createFAQ(FAQ faq, Pengguna sender);
    FAQ jawabFAQ(FAQ newFaq);
    FAQ eskalasiFAQ(int idFAQ);
    FAQ editFAQ(FAQ newFaq);
    FAQ deleteFAQ(int idFAQ);
    List<FAQ> getFaqsByStatus(int status);
    List<FAQ> getFaqsByStatusAndSearch(String search, int status);
    FAQ getFAQbyId(int idFAQ);
    List<FAQ> getAllNotAnsweredFaq();
    List<FAQ> getAllEskalasiFaq();
    List<FAQ> getAllAnsweredFaq();
    List<FAQ> getAllDeletedFaq();
    Map<String, Long> getCountOfAnsweredQuestions(Pengguna pengaju);
    List<FAQ> getFaqsByPengajuAndStatus(Pengguna pengaju, int status);
} 

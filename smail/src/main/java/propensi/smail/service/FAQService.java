package propensi.smail.service;

import propensi.smail.model.FAQ;
import java.util.*;

public interface FAQService {
    void createFAQ(FAQ faq);
    FAQ jawabFAQ(FAQ newFaq);
    FAQ eskalasiFAQ(int idFAQ);
    FAQ editFAQ(FAQ newFaq);
    FAQ deleteFAQ(int idFAQ);
    List<FAQ> getFaqsByStatus(int status);
    FAQ getFAQbyId(int idFAQ);
} 
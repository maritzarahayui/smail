package propensi.smail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.smail.model.FAQ;

@Repository
public interface FAQDb extends JpaRepository<FAQ, Integer> {
    
}

package propensi.smail.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.smail.model.FAQ;

@Repository
public interface FAQDb extends JpaRepository<FAQ, Integer> {
    FAQ findById(int id);
    List<FAQ> findAll();
    List<FAQ> findByStatus(int status);
}

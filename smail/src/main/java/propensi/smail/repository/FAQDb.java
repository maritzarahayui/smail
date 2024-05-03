package propensi.smail.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.smail.model.FAQ;
import propensi.smail.model.user.*;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface FAQDb extends JpaRepository<FAQ, Integer> {
    FAQ findById(int id);
    List<FAQ> findAll();
    List<FAQ> findByStatus(int status);
    List<FAQ> findByPertanyaanContainingIgnoreCaseAndStatus(String search, int status);
    long countFAQByPengajuAndStatus(Pengguna pengaju, int status);

}
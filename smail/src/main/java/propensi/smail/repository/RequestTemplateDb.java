package propensi.smail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import propensi.smail.model.RequestTemplate;
import java.util.*;

@Repository
public interface RequestTemplateDb extends JpaRepository<RequestTemplate, String> {
    List<RequestTemplate> findByStatus(int status);
    @Query("SELECT r FROM RequestTemplate r WHERE " +
            "LOWER(r.pengaju.nama) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.kategori) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.id) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<RequestTemplate> findByKeyword(@Param("keyword") String keyword);
}

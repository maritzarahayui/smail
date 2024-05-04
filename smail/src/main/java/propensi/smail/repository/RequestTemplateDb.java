package propensi.smail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.smail.model.RequestTemplate;

import java.util.List;

@Repository
public interface RequestTemplateDb extends JpaRepository<RequestTemplate, String> {
    List<RequestTemplate> findByStatus(int status);
}

package propensi.smail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.smail.model.RequestSurat;

@Repository
public interface RequestSuratDb extends JpaRepository<RequestSurat, String> {
    
}

package propensi.smail.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import propensi.smail.model.Pengguna;
import propensi.smail.model.RequestSurat;
import propensi.smail.model.Role;

@Repository
public interface RequestSuratDb extends JpaRepository<RequestSurat, String> {
    // long countByPengajuRole(Role role);
    // List<RequestSurat> findByPengaju(Pengguna pengaju);
}

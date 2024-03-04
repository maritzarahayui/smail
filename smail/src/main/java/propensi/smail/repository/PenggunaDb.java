package propensi.smail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.smail.model.user.Pengguna;

@Repository
public interface PenggunaDb extends JpaRepository<Pengguna, String> {
    Pengguna findByEmail(String email);
} 
package propensi.smail.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import propensi.smail.model.user.Pengguna;

@Repository
public interface PenggunaDb extends JpaRepository<Pengguna, String> {
    Optional<Pengguna> findByEmail(String email);
} 
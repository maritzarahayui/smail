package propensi.smail.service;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import propensi.smail.model.user.*;
import propensi.smail.repository.PenggunaDb;

@Service
public class PenggunaServiceImpl implements PenggunaService {

    @Autowired
    PenggunaDb penggunaDb;

    @Override
    public Pengguna getPenggunaLoggedIn(String email) {
        Optional<Pengguna> existUser = penggunaDb.findByEmail(email);
        Pengguna user = existUser.orElseThrow(() -> new EntityNotFoundException("Pengguna tidak ditemukan"));
        return user;
    }

    @Override
    public String getRole(Pengguna user) {
        if (user instanceof Admin) {
            return "Admin";
        } else if (user instanceof Pengurus) {
            return "Pengurus";
        } else if (user instanceof Dosen) {
            return "Dosen";
        } else if (user instanceof Staf) {
            return "Staf";
        } else if (user instanceof Mahasiswa) {
            return "Mahasiswa";
        } return "";
    }

    @Override
    public String getFirstName(Pengguna user) {
        String fullName = user.getNama();
        String firstName = fullName.split(" ")[0];
        return firstName;
    }

    @Override
    public Pengguna getPenggunaById(String id) {
        Optional<Pengguna> existUser = penggunaDb.findById(id);
        Pengguna user = existUser.orElseThrow(() -> new EntityNotFoundException("Pengguna tidak ditemukan"));
        return user;
    }

    @Override
    public List<String> getAllRoles() {
        return Arrays.asList("Dosen", "Staf", "Mahasiswa");
    }
}

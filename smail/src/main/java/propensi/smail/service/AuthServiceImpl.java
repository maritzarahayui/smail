package propensi.smail.service;

import java.io.*;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import propensi.smail.model.user.*;
import propensi.smail.repository.PenggunaDb;

@Service
public class AuthServiceImpl implements AuthService {

    static final String CSV_FILE_PATH = "src/main/resources/static/tazkia.csv";

    @Autowired
    PenggunaDb penggunaDb;

    @Override
    public void importDataPengguna() throws FileNotFoundException, IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                String email = data[0];
                String id = data[1];
                String nama = data[2];
                String role = data[3];

                System.out.println("email: " + email);
                System.out.println("role: " + role);
                System.out.println("id: " + id);

                Pengguna pengguna = null;

                if (role.equalsIgnoreCase("Admin")) {
                    pengguna = new Admin();
                } else if (role.equalsIgnoreCase("Pengurus")) {
                    pengguna = new Pengurus();
                } else if (role.equalsIgnoreCase("Dosen")) {
                    pengguna = new Dosen();
                } else if (role.equalsIgnoreCase("Staf")) {
                    pengguna = new Staf();
                } else if (role.equalsIgnoreCase("Mahasiswa")) {
                    pengguna = new Mahasiswa();
                }
                
                pengguna.setId(id);
                pengguna.setEmail(email);
                pengguna.setNama(nama);
                penggunaDb.save(pengguna);
            }
        }
    }

    @Override
    public boolean validatePengguna(String email) {
        Optional<Pengguna> existUser = penggunaDb.findByEmail(email);
        return existUser.isPresent();
    }

}

   
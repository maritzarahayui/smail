package propensi.smail.service;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface AuthService {

    void importDataPengguna() throws FileNotFoundException, IOException;
    boolean validatePengguna(String email);

}

package propensi.smail.service;

import propensi.smail.model.user.*;

public interface PenggunaService {
    Pengguna getPenggunaLoggedIn(String email);
    String getRole(Pengguna user);
} 
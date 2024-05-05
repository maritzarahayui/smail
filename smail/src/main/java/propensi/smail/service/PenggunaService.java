package propensi.smail.service;

import java.util.List;

import propensi.smail.model.user.*;

public interface PenggunaService {
    Pengguna getPenggunaLoggedIn(String email);
    String getRole(Pengguna user);
    String getFirstName(Pengguna user);
    Pengguna getPenggunaById(String id);
    List<String> getAllRoles();
} 
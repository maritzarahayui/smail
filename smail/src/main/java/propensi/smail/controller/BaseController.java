package propensi.smail.controller;

import org.springframework.ui.Model;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BaseController {
    
    @GetMapping("/")
    public String home(Model model) {
        return "home";
    }

    @GetMapping("/secured")
    public String secured() {
        return "logged-in";
    }

    // @GetMapping("/login")
    // public String login(Authentication authentication) throws IOException {
    //     if (authentication instanceof OAuth2AuthenticationToken) {
    //         OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
    //         String email = oauthToken.getPrincipal().getAttribute("email");
    //         // Menggunakan role default jika tidak ada informasi role dari token OAuth2
    //         String role = "default";
    //         if (csvDataReader.isValidUser(email, role)) {
    //             // Login berhasil
    //             return "redirect:/home";
    //         } else {
    //             // Akun tidak valid, tampilkan pesan kesalahan
    //             return "redirect:/error?message=Invalid account, please use a registered account.";
    //         }
    //     } else {
    //         // Tidak didukung untuk autentikasi selain OAuth2
    //         return "redirect:/error?message=Unsupported authentication method.";
    //     }
    // }
}
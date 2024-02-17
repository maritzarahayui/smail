package propensi.smail.controller;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SuratController {
    
    @GetMapping("/")
    public String home(Model model) {
        return "home";
    }
}
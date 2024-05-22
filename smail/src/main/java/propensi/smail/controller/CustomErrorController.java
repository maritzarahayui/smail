package propensi.smail.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            model.addAttribute("errorCode", statusCode);
            model.addAttribute("errorMessage", getCustomMessage(statusCode));
        }
        return "error";
    }

    private String getCustomMessage(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Permintaan tidak valid. Mohon periksa kembali format dan parameter yang dikirim.";
            case 403:
                return "Akses ditolak. Anda tidak memiliki izin untuk mengakses sumber daya ini.";
            case 404:
                return "Sumber daya yang Anda cari tidak ditemukan.";
            case 408:
                return "Waktu permintaan telah habis.";
            case 413:
                return "Ukuran permintaan terlalu besar.";
            case 415:
                return "Tipe media dari permintaan tidak didukung.";
            case 500:
                return "Terjadi kesalahan internal server.";
            case 504:
                return "Gateway time-out. Server tidak menerima respons tepat waktu.";
            case 505:
                return "Versi HTTP yang digunakan dalam permintaan tidak didukung oleh server.";
            case 507:
                return "Server kehabisan ruang penyimpanan.";
            default:
                return "Terjadi kesalahan tidak diketahui.";
        }
    }
    
}

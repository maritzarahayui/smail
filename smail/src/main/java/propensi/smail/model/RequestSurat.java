package propensi.smail.model;

import lombok.*;
import java.util.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "request_surat")

public class RequestSurat {
    
    @Id
    @GeneratedValue
    private Integer id;

    @NotNull
    @Column(name = "jenis_surat", nullable = false)
    private String jenisSurat;

    @NotNull
    @Column(name = "status", nullable = false)
    private Integer status;

    @NotNull
    @Column(name = "tanggal_pengajuan", nullable = false)
    private Date tanggalPengajuan;

    @NotNull
    @Column(name = "tanggal_selesai", nullable = false)
    private Date tanggalSelesai;

    @NotNull
    @Column(name = "alasan_penolakan", nullable = false)
    private String alasanPenolakan;

    @NotNull
    @Column(name = "form_value", nullable = false)
    private Map<String,String> formValue;

    @NotNull
    @Column(name = "user", nullable = false)
    private User user;

    @NotNull
    @Column(name = "surat", nullable = false)
    private SuratKeluar surat;

}

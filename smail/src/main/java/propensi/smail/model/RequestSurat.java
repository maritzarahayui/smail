package propensi.smail.model;

import lombok.*;
import java.util.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "request_surat")

public class RequestSurat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "jenis_surat", nullable = false)
    private String jenisSurat;

    @NotNull
    @Column(name = "status", nullable = false)
    private int status;

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

    @NotNull // many to one
    @Column(name = "user", nullable = false)
    private User user;

    @NotNull // one to one
    @Column(name = "surat", nullable = false)
    private SuratKeluar surat;

}

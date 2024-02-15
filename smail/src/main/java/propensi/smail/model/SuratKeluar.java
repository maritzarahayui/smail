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
@Table(name = "surat_keluar")

public class SuratKeluar {
    
    @Id
    @GeneratedValue
    private String no_surat;

    @NotNull
    @Column(name = "jenis_surat", nullable = false)
    private String jenisSurat;

    @NotNull
    @Column(name = "status", nullable = false)
    private Integer status;

    @NotNull
    @Column(name = "tanggal_dibuat", nullable = false)
    private Date tanggalDibuat;

    // @NotNull
    // @Column(name = "file", nullable = false)
    // private Integer file;

    @NotNull
    @Column(name = "request_surat", nullable = false)
    private RequestSurat requestSurat;

    @NotNull
    @Column(name = "surat_masuk", nullable = false)
    private SuratMasuk suratMasuk;

}

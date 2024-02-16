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
@Table(name = "surat_masuk")

public class SuratMasuk {
    
    @Id
    private String noSurat;

    @NotNull
    @Column(name = "jenis_surat", nullable = false)
    private String jenisSurat;

    @NotNull
    @Column(name = "status", nullable = false)
    private int status;

    @NotNull
    @Column(name = "tanggal_dibuat", nullable = false)
    private Date tanggalDibuat;

    // @NotNull
    // @Column(name = "file", nullable = false)
    // private int file;

    @NotNull  // one to one
    @Column(name = "surat_keluar", nullable = false)
    private SuratKeluar suratKeluar;

}

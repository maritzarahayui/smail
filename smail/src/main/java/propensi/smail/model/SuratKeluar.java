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
@Table(name = "surat_keluar")

public class SuratKeluar {
    
    @Id
    @Column(name = "nomor_arsip", nullable = false)
    private String nomorArsip;

    @NotNull
    @Column(name = "kategori", nullable = false)
    private String kategori;

    @Column(name = "jenis_surat")
    private String jenisSurat;

    @NotNull
    @Column(name = "perihal", nullable = false)
    private String perihal;

    // @Column(name = "penerima")
    // private User penerima;  // pending dl ya msh mikir 

    @Column(name = "penerima_eksternal")
    private String penerimaEksternal; // email

    @NotNull
    @Column(name = "tanggal_dibuat", nullable = false)
    private Date tanggalDibuat;

    @Lob
    @Column(name = "file", nullable = false)
    private byte[] file;

    @NotNull
    @Column(name = "file_name")
    private String fileName; // filename

    /* RELATIONSHIPS */
    // @ManyToOne
    // @JoinColumn(name = "penandatangan")
    // private Pengguna penandatangan;

}

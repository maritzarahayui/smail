package propensi.smail.model;

import lombok.*;
import java.util.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import propensi.smail.model.user.Pengguna;

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

    @Column(name = "perihal")
    private String perihal = "Permohonan request surat";

    // @Column(name = "penerima")
    // private User penerima;  // pending dl ya msh mikir

    @Column(name = "penerima_eksternal")
    private String penerimaEksternal = " "; // email

    @NotNull
    @Column(name = "tanggal_dibuat", nullable = false)
    private Date tanggalDibuat;

    @Lob
    @Column(name = "file", nullable = false)
    private byte[] file;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "isSigned")
    private Boolean isSigned = false;

    @ManyToMany
    @JoinTable(
            name = "suratkeluar_pengguna",
            joinColumns = @JoinColumn(name = "surat_keluar_id"),
            inverseJoinColumns = @JoinColumn(name = "pengguna_id")
    )
    private List<Pengguna> penandatangan;

    @ManyToOne
    @JoinColumn(name = "current_penandatangan")
    private Pengguna currentPenandatangan;

    @ManyToOne
    @JoinColumn(name = "pengaju")
    private Pengguna pengaju;

    @OneToOne
    @JoinColumn(name = "surat_id")
    private RequestSurat requestSurat;

    @OneToOne
    @JoinColumn(name = "arsip_id")
    private SuratMasuk arsipSurat;
}

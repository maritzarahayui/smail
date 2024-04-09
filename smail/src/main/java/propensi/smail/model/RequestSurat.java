package propensi.smail.model;

import lombok.*;
import propensi.smail.model.user.Pengguna;

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
    private String id;

    @NotNull
    @Column(name = "kategori", nullable = false)
    private String kategori;

    @NotNull
    @Column(name = "jenis_surat", nullable = false)
    private String jenisSurat;

    @NotNull
    @Column(name = "bentuk_surat", nullable = false)
    private String bentukSurat;

    @NotNull
    @Column(name = "bahasa", nullable = false)
    private String bahasa;

    @NotNull
    @Column(name = "keperluan", nullable = false)
    private String keperluan;

    @NotNull
    @Column(name = "status", nullable = false)
    private int status;

    @Column(name = "tanggal_pengajuan", nullable = false)
    private Date tanggalPengajuan;

    @Column(name = "tanggal_selesai")
    private Date tanggalSelesai;

    @Column(name = "tanggal_penolakan")
    private Date tanggalPenolakan;

    @Column(name = "alasan_penolakan")
    private String alasanPenolakan;

    @Column(name = "tanggal_dibatalkan")
    private Date tanggalDibatalkan;

    @Column(name = "alasan_pembatalan")
    private String alasanPembatalan;

    
    /* RELATIONSHIPS */
    @ManyToOne
    @JoinColumn(name = "pengaju")
    private Pengguna pengaju; 
    
    @OneToMany(mappedBy = "requestSurat", cascade = CascadeType.ALL)
    private List<FieldData> listFieldData;

    @ManyToOne
    @JoinColumn(name = "template")
    private TemplateSurat template;

    @OneToOne
    @JoinColumn(name = "surat")
    private SuratKeluar surat;

}

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
    // private String id;
    private UUID id = UUID.randomUUID();

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

    @NotNull
    @Column(name = "tanggal_pengajuan", nullable = false)
    private Date tanggalPengajuan;

    @Column(name = "tanggal_selesai")
    private Date tanggalSelesai;

    @Column(name = "alasan_penolakan")
    private String alasanPenolakan;

    @Column(name = "alasan_pembatalan")
    private String alasanPembatalan;

    
    /* RELATIONSHIPS */
    // @ManyToOne
    // @JoinColumn(name = "pengaju")
    // private User pengaju;  // pending dl ya msh mikir 

    // @ManyToOne
    // @JoinColumn(name = "pengaju")
    // private Pengguna pengaju;
    
    // @OneToMany(mappedBy = "requestSurat", cascade = CascadeType.ALL)
    // private List<FieldData> listFieldData;

    // @ManyToOne
    // @JoinColumn(name = "template")
    // private TemplateSurat template;

    // @OneToOne
    // @JoinColumn(name = "surat")
    // private SuratKeluar surat;

}

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
@Table(name = "request_template")

public class RequestTemplate {
    
//    @Id
//    private String id;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "kategori", nullable = false)
    private String kategori;

    @NotNull
    @Column(name = "bahasa", nullable = false)
    private String bahasa;

    @NotNull
    @Column(name = "keperluan", nullable = false)
    private String keperluan;

    @NotNull
    @Column(name = "status", nullable = false)
    private int status;

    // @NotNull
    // @Column(name = "pengaju", nullable = false)
    // private User pengaju;  // pending dl ya msh mikir 

    @NotNull
    @Column(name = "tanggal_pengajuan", nullable = false)
    private Date tanggalPengajuan;

    @Column(name = "tanggal_selesai")
    private Date tanggalSelesai;

    @Column(name = "alasan_penolakan")
    private String alasanPenolakan;

    
    /* RELATIONSHIPS */
    @OneToMany(mappedBy = "requestTemplate", cascade = CascadeType.ALL)
    private List<FieldData> listFieldData;
    
}

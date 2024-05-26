package propensi.smail.model;

import lombok.*;
import propensi.smail.model.user.Pengguna;

import java.util.*;
import org.hibernate.annotations.GenericGenerator;

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
    @Column(name = "nomor_arsip", nullable = false)
    private String nomorArsip;

    @NotNull
    @Column(name = "kategori", nullable = false)
    private String kategori;

    @NotNull
    @Column(name = "perihal", nullable = false)
    private String perihal;

    @NotNull
    @Column(name = "tanggal_dibuat", nullable = false)
    private Date tanggalDibuat;

    @Column(name = "is_disposisi")
    private Boolean isDisposisi = false;

    @Column(name = "is_followup")
    private Boolean isFollowUp = false;

    @NotNull
    @Column(name = "pengirim", nullable = false)
    private String pengirim; // email or nama pengirim

    @Column(name = "tembusan")
    private ArrayList<String> tembusan = new ArrayList<String>(); // email

    @Lob
    @Column(name = "file", nullable = false)
    private byte[] file; 

    @NotNull
    @Column(name = "file_name")
    private String fileName; // filename
 
    /* RELATIONSHIPS */
    @OneToOne
    @JoinColumn(name = "surat_follow_up")
    private SuratKeluar suratFollowUp;

    @ManyToOne
    @JoinColumn(name = "penandatangan")
    private Pengguna penandatangan;

}

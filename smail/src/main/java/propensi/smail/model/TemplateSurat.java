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
@Table(name = "template_surat")

public class TemplateSurat {
  
    @Id
    private String id;

    @NotNull
    @Column(name = "kategori", nullable = false)
    private String kategori;

    @NotNull
    @Column(name = "nama_template", nullable = false)
    private String namaTemplate;

    @NotNull
    @Column(name = "tanggal_dibuat", nullable = false)
    private Date tanggalDibuat;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @NotNull
    @Column(name = "list_pengguna", nullable = false)
    private ArrayList<String> listPengguna;

    @NotNull
    @Column(name = "list_field", nullable = false)
    private ArrayList<String> listField;

    @Lob
    @Column(name = "file", nullable = false)
    private byte[] file;


    /* RELATIONSHIPS */
  

}

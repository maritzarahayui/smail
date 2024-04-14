package propensi.smail.model;

import lombok.*;
import java.util.Date;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "faq")

public class FAQ {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Column(name = "pertanyaan", nullable = false)
    private String pertanyaan;

    @Column(name = "jawaban")
    private String jawaban;

    /* STATUS
     * 0 : Belum dijawab
     * 1 : Dieskalasi
     * 2 : Terjawab
     * 3 : Dihapus */
    @NotNull
    @Column(name = "status", nullable = false)
    private int status;

    @Column(name = "tanggal_post")
    private Date tanggalPost;

}

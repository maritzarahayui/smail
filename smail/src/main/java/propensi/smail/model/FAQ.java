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

    @NotNull
    @Column(name = "jawaban", nullable = false)
    private String jawaban;

    @NotNull
    @Column(name = "status", nullable = false)
    private int status;

    @NotNull
    @Column(name = "tanggal_post", nullable = false)
    private Date tanggalPost;

}
